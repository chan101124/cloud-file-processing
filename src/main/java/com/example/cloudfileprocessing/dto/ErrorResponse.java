package com.example.cloudfileprocessing.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final String message;
    private final String path;

    public ErrorResponse(LocalDateTime timestamp, String message, String path) {
        this.timestamp = timestamp;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
