package com.example.ragApplication.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class VectorStoreConfig {

    private final EmbeddingModel embeddingModel;
    private final DataSource dataSource;


    @Autowired
    public VectorStoreConfig(EmbeddingModel embeddingModel, DataSource dataSource) {
        this.embeddingModel = embeddingModel;
        this.dataSource = dataSource;
    }

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel){
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(768)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .vectorTableName("vector_store")
                .build();
    }
}
