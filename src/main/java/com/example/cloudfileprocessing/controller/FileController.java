package com.example.cloudfileprocessing.controller;

import com.example.cloudfileprocessing.dto.FileMetadataResponse;
import com.example.cloudfileprocessing.dto.FileUploadResponse;
import com.example.cloudfileprocessing.service.FileService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") @NotNull MultipartFile file) {
        log.info("Upload request received: originalFilename='{}', size={} bytes, contentType='{}'",
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        FileUploadResponse response = fileService.uploadFile(file);
        log.info("Upload completed: fileId={}, fileName='{}', processingStatus={}",
                response.getId(), response.getFilename(), response.getProcessingStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FileMetadataResponse>> listFiles() {
        log.info("List files request received");
        List<FileMetadataResponse> files = fileService.listFiles();
        log.info("List files completed: totalFiles={}", files.size());
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        log.info("Download request received: id={}", id);
        Resource resource = fileService.downloadFile(id);
        log.info("Download prepared: id={}, resource='{}'", id, resource.getFilename());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file-" + id + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
