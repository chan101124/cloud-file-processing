package com.example.cloudfileprocessing.dto;

public class FileQueueMessage {

    private Long id;
    private String filename;
    private String s3Key;

    public FileQueueMessage() {
    }

    public FileQueueMessage(Long id, String filename, String s3Key) {
        this.id = id;
        this.filename = filename;
        this.s3Key = s3Key;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }
}
