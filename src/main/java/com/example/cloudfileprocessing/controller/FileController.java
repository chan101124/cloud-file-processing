package com.example.cloudfileprocessing.controller;

import com.example.cloudfileprocessing.dto.FileMetadataResponse;
import com.example.cloudfileprocessing.dto.FileUploadResponse;
import com.example.cloudfileprocessing.service.FileService;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
@Validated
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") @NotNull MultipartFile file) {
        FileUploadResponse response = fileService.uploadFile(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FileMetadataResponse>> listFiles() {
        return ResponseEntity.ok(fileService.listFiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Resource resource = fileService.downloadFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file-" + id + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
