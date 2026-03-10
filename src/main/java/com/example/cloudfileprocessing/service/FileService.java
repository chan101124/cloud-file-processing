package com.example.cloudfileprocessing.service;

import com.example.cloudfileprocessing.dto.FileMetadataResponse;
import com.example.cloudfileprocessing.dto.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    FileUploadResponse uploadFile(MultipartFile file);

    List<FileMetadataResponse> listFiles();

    Resource downloadFile(Long id);
}
