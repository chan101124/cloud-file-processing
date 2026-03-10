package com.example.cloudfileprocessing.repository;

import com.example.cloudfileprocessing.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}
