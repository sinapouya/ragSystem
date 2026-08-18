package com.example.docMind.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        try {
            log.info("🚀 Initializing MinIO client with endpoint: {}", minioProperties.getEndpoint());
            return MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
        } catch (Exception e) {
            log.error("❌ Failed to create MinIO client: {}", e.getMessage());
            throw new RuntimeException("MinIO client initialization failed", e);
        }
    }
}