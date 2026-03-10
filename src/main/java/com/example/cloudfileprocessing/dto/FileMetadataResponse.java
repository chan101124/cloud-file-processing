package com.example.cloudfileprocessing.dto;

import com.example.cloudfileprocessing.model.ProcessingStatus;

import java.time.LocalDateTime;

public class FileMetadataResponse {

    private Long id;
    private String filename;
    private LocalDateTime uploadTime;
    private ProcessingStatus processingStatus;

    public FileMetadataResponse(Long id, String filename, LocalDateTime uploadTime, ProcessingStatus processingStatus) {
        this.id = id;
        this.filename = filename;
        this.uploadTime = uploadTime;
        this.processingStatus = processingStatus;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }
}
