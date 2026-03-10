package com.example.cloudfileprocessing.service;

import com.example.cloudfileprocessing.exception.MessagingException;
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
            throw new MessagingException("Failed to delete message from SQS", ex);
        }
    }
}
