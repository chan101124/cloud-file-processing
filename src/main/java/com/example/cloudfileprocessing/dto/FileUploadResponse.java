package com.example.cloudfileprocessing.dto;

import com.example.cloudfileprocessing.model.ProcessingStatus;

public class FileUploadResponse {

    private final Long id;
    private final String filename;
    private final ProcessingStatus processingStatus;

    public FileUploadResponse(Long id, String filename, ProcessingStatus processingStatus) {
        this.id = id;
        this.filename = filename;
        this.processingStatus = processingStatus;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }
}
