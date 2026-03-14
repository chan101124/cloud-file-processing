package com.example.cloudfileprocessing.service;

import com.example.cloudfileprocessing.exception.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.List;

@Service
public class SqsMessagingService {

    private static final Logger log = LoggerFactory.getLogger(SqsMessagingService.class);

    private final SqsClient sqsClient;

    public SqsMessagingService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void sendMessage(String queueUrl, String payload) {
        try {
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(payload)
                    .build();
            sqsClient.sendMessage(request);
        } catch (SqsException ex) {
            log.error("SQS sendMessage failed: queueUrl='{}', statusCode={}, errorCode='{}', requestId='{}', awsMessage='{}'",
                    queueUrl,
                    ex.statusCode(),
                    getSafeErrorCode(ex),
                    getSafeRequestId(ex),
                    getSafeErrorMessage(ex),
                    ex);
            throw new MessagingException("Failed to send message to SQS", ex);
        }
    }

    public List<Message> pollMessages(String queueUrl, int maxMessages, int waitTimeSeconds) {
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(maxMessages)
                    .waitTimeSeconds(waitTimeSeconds)
                    .build();
            return sqsClient.receiveMessage(request).messages();
        } catch (SqsException ex) {
            log.error("SQS receiveMessage failed: queueUrl='{}', statusCode={}, errorCode='{}', requestId='{}', awsMessage='{}'",
                    queueUrl,
                    ex.statusCode(),
                    getSafeErrorCode(ex),
                    getSafeRequestId(ex),
                    getSafeErrorMessage(ex),
                    ex);
            throw new MessagingException("Failed to poll messages from SQS", ex);
        }
    }

    public void deleteMessage(String queueUrl, String receiptHandle) {
        try {
            DeleteMessageRequest request = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();
            sqsClient.deleteMessage(request);
        } catch (SqsException ex) {
            log.error("SQS deleteMessage failed: queueUrl='{}', statusCode={}, errorCode='{}', requestId='{}', awsMessage='{}'",
                    queueUrl,
                    ex.statusCode(),
                    getSafeErrorCode(ex),
                    getSafeRequestId(ex),
                    getSafeErrorMessage(ex),
                    ex);
            throw new MessagingException("Failed to delete message from SQS", ex);
        }
    }

    private String getSafeErrorCode(SqsException ex) {
        return ex.awsErrorDetails() != null ? ex.awsErrorDetails().errorCode() : "N/A";
    }

    private String getSafeErrorMessage(SqsException ex) {
        return ex.awsErrorDetails() != null ? ex.awsErrorDetails().errorMessage() : ex.getMessage();
    }

    private String getSafeRequestId(SqsException ex) {
        return ex.requestId() != null ? ex.requestId() : "N/A";
    }
}
