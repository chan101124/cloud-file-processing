package com.example.cloudfileprocessing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
public class AwsProperties {

    private String region;
    private String s3BucketName;
    private String sqsQueueUrl;
    private String accessKey;
    private String secretAccessKey;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public String getSqsQueueUrl() {
        return sqsQueueUrl;
    }

    public void setSqsQueueUrl(String sqsQueueUrl) {
        this.sqsQueueUrl = sqsQueueUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }
}
