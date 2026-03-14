package com.example.cloudfileprocessing.service.impl;

import com.example.cloudfileprocessing.config.AwsProperties;
import com.example.cloudfileprocessing.dto.FileQueueMessage;
import com.example.cloudfileprocessing.dto.FileMetadataResponse;
import com.example.cloudfileprocessing.dto.FileUploadResponse;
import com.example.cloudfileprocessing.exception.MessagingException;
import com.example.cloudfileprocessing.exception.ResourceNotFoundException;
import com.example.cloudfileprocessing.model.FileMetadata;
import com.example.cloudfileprocessing.model.ProcessingStatus;
import com.example.cloudfileprocessing.repository.FileMetadataRepository;
import com.example.cloudfileprocessing.service.FileService;
import com.example.cloudfileprocessing.service.S3StorageService;
import com.example.cloudfileprocessing.service.SqsMessagingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileMetadataRepository fileMetadataRepository;
    private final S3StorageService s3StorageService;
    private final SqsMessagingService sqsMessagingService;
    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper;

    public FileServiceImpl(FileMetadataRepository fileMetadataRepository,
                           S3StorageService s3StorageService,
                           SqsMessagingService sqsMessagingService,
                           AwsProperties awsProperties,
                           ObjectMapper objectMapper) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.s3StorageService = s3StorageService;
        this.sqsMessagingService = sqsMessagingService;
        this.awsProperties = awsProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty");
        }

        String originalFilename = file.getOriginalFilename() == null ? "unnamed-file" : file.getOriginalFilename();
        String s3Key = UUID.randomUUID() + "-" + originalFilename;

        log.info("Uploading file '{}' to S3 key '{}'", originalFilename, s3Key);
        s3StorageService.uploadFile(awsProperties.getS3BucketName(), s3Key, file);

        FileMetadata metadata = new FileMetadata();
        metadata.setFilename(originalFilename);
        metadata.setS3Key(s3Key);
        metadata.setProcessingStatus(ProcessingStatus.UPLOADED);
        metadata = fileMetadataRepository.save(metadata);

        String messagePayload = buildUploadMessage(metadata);
        try {
            log.info("Sending SQS message for file id={} to queueUrl='{}'", metadata.getId(), awsProperties.getSqsQueueUrl());
            sqsMessagingService.sendMessage(awsProperties.getSqsQueueUrl(), messagePayload);
            metadata.setProcessingStatus(ProcessingStatus.QUEUED);
            fileMetadataRepository.save(metadata);
            log.info("Queued file id={} for processing", metadata.getId());
        } catch (MessagingException ex) {
            metadata.setProcessingStatus(ProcessingStatus.FAILED);
            fileMetadataRepository.save(metadata);
            log.error("Failed to queue file id={} for processing. Marked status=FAILED", metadata.getId(), ex);
            throw ex;
        }

        return new FileUploadResponse(metadata.getId(), metadata.getFilename(), metadata.getProcessingStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileMetadataResponse> listFiles() {
        return fileMetadataRepository.findAll()
                .stream()
                .map(file -> new FileMetadataResponse(
                        file.getId(),
                        file.getFilename(),
                        file.getUploadTime(),
                        file.getProcessingStatus()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(Long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File metadata not found for id " + id));
        return s3StorageService.downloadFile(awsProperties.getS3BucketName(), metadata.getS3Key());
    }

    private String buildUploadMessage(FileMetadata metadata) {
        try {
            FileQueueMessage payload = new FileQueueMessage(
                    metadata.getId(),
                    metadata.getFilename(),
                    metadata.getS3Key());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new MessagingException("Failed to build SQS payload", ex);
        }
    }
}
