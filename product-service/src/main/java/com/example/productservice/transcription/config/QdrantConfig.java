
package com.example.productservice.transcription.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {

    @Bean
    public QdrantClient qdrantClient() {
        return new QdrantClient(
                QdrantGrpcClient.newBuilder("127.0.0.1", 6334, false).build()
        );
    }
}
