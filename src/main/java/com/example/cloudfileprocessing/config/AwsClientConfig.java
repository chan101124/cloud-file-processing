package com.example.cloudfileprocessing.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsClientConfig {

    @Bean
    public S3Client s3Client(AwsProperties awsProperties) {
        return S3Client.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider(awsProperties))
                .build();
    }

    @Bean
    public SqsClient sqsClient(AwsProperties awsProperties) {
        return SqsClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider(awsProperties))
                .build();
    }

    private AwsCredentialsProvider credentialsProvider(AwsProperties awsProperties) {
        String accessKey = awsProperties.getAccessKey();
        String secretAccessKey = awsProperties.getSecretAccessKey();

        if (hasText(accessKey) && hasText(secretAccessKey)) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretAccessKey);
            return StaticCredentialsProvider.create(credentials);
        }

        return DefaultCredentialsProvider.create();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
