package com.sidwik.logquery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class AwsConfig {
    @Bean
    public S3Client s3Client(LogQueryProperties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.aws().endpoint()))
                .region(Region.of(properties.aws().region()))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")
                ))
                .build();
    }
}
