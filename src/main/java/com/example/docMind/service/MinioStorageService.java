package com.example.docMind.service;

import com.example.docMind.config.MinioProperties;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * Uploads a file to MinIO and returns the object key (path).
     */
    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String objectKey = "documents/" + UUID.randomUUID() + extension;

            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(),null)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("✅ File uploaded to MinIO: {}", objectKey);
            return objectKey;

        } catch (Exception e) {
            log.error("❌ Failed to upload file to MinIO: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to storage: " + e.getMessage(), e);
        }
    }
    public String uploadTextContent(String text, String filename) {
        try {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            String objectKey = "documents/" + filename;

            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .stream(new ByteArrayInputStream(bytes), Long.valueOf(bytes.length), null)
                            .contentType("text/plain; charset=utf-8")
                            .build()
            );

            log.info("✅ Text uploaded to MinIO as: {}", objectKey);
            return objectKey;

        } catch (Exception e) {
            log.error("❌ Failed to upload raw text to MinIO: {}", e.getMessage());
            throw new RuntimeException("Failed to upload raw text: " + e.getMessage(), e);
        }
    }
    /**
     * Downloads a file from MinIO.
     */
    public InputStream downloadFile(String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Failed to download file from MinIO: {}", e.getMessage());
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a pre-signed URL for temporary access.
     */
    public String getPresignedUrl(String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .method(Http.Method.GET)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Failed to generate pre-signed URL: {}", e.getMessage());
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    /**
     * Deletes a file from MinIO.
     */
    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .build()
            );
            log.info("🗑️ File deleted from MinIO: {}", objectKey);
        } catch (Exception e) {
            log.error("❌ Failed to delete file from MinIO: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    /**
     * Checks if a file exists in MinIO.
     */
    public boolean fileExists(String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ============ Private Helpers ============

    private void ensureBucketExists() {
        try {
            String bucketName = minioProperties.getBucketName();
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("📦 Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("Could not verify/create bucket: {}", e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(dot).toLowerCase() : "";
    }
}