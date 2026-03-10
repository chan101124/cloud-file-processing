package com.example.cloudfileprocessing.worker;

import com.example.cloudfileprocessing.config.AwsProperties;
import com.example.cloudfileprocessing.dto.FileQueueMessage;
import com.example.cloudfileprocessing.model.FileMetadata;
import com.example.cloudfileprocessing.model.ProcessingStatus;
import com.example.cloudfileprocessing.repository.FileMetadataRepository;
import com.example.cloudfileprocessing.service.SqsMessagingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.Optional;

@Component
public class SqsFileProcessingWorker {

    private static final Logger log = LoggerFactory.getLogger(SqsFileProcessingWorker.class);
    private final SqsMessagingService sqsMessagingService;
    private final FileMetadataRepository fileMetadataRepository;
    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper;

    public SqsFileProcessingWorker(SqsMessagingService sqsMessagingService,
                                   FileMetadataRepository fileMetadataRepository,
                                   AwsProperties awsProperties,
                                   ObjectMapper objectMapper) {
        this.sqsMessagingService = sqsMessagingService;
        this.fileMetadataRepository = fileMetadataRepository;
        this.awsProperties = awsProperties;
        this.objectMapper = objectMapper;
    }

    // Long polling reduces API calls and cost while waiting for messages.
    @Scheduled(fixedDelayString = "${app.worker.poll-delay-ms:5000}")
    @Transactional
    public void pollAndProcess() {
        List<Message> messages = sqsMessagingService.pollMessages(awsProperties.getSqsQueueUrl(), 10, 10);
        if (messages.isEmpty()) {
            return;
        }

        log.info("Worker received {} messages from SQS", messages.size());
        for (Message message : messages) {
            processSingleMessage(message);
        }
    }

    private void processSingleMessage(Message message) {
        try {
            FileQueueMessage payload = objectMapper.readValue(message.body(), FileQueueMessage.class);
            Long id = payload.getId();
            if (id == null) {
                throw new IllegalArgumentException("Message payload does not contain file id");
            }

            Optional<FileMetadata> metadataOptional = fileMetadataRepository.findById(id);
            if (metadataOptional.isEmpty()) {
                log.warn("Metadata not found for id={}, deleting message", id);
                sqsMessagingService.deleteMessage(awsProperties.getSqsQueueUrl(), message.receiptHandle());
                return;
            }

            FileMetadata metadata = metadataOptional.get();
            metadata.setProcessingStatus(ProcessingStatus.PROCESSING);
            fileMetadataRepository.save(metadata);

            // Demo processing logic. Replace this with real processing in production.
            metadata.setProcessingStatus(ProcessingStatus.PROCESSED);
            fileMetadataRepository.save(metadata);

            sqsMessagingService.deleteMessage(awsProperties.getSqsQueueUrl(), message.receiptHandle());
            log.info("Successfully processed file id={}", id);
        } catch (Exception ex) {
            log.error("Failed to process message {}", message.messageId(), ex);
            markAsFailed(message.body());
        }
    }

    private void markAsFailed(String messageBody) {
        try {
            FileQueueMessage payload = objectMapper.readValue(messageBody, FileQueueMessage.class);
            Long id = payload.getId();
            if (id == null) {
                return;
            }
            fileMetadataRepository.findById(id).ifPresent(metadata -> {
                metadata.setProcessingStatus(ProcessingStatus.FAILED);
                fileMetadataRepository.save(metadata);
            });
        } catch (Exception ex) {
            log.error("Failed to mark metadata as FAILED for message body {}", messageBody, ex);
        }
    }
}
