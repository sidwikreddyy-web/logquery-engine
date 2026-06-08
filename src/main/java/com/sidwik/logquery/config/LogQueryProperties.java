package com.sidwik.logquery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logquery")
public record LogQueryProperties(
        Kafka kafka,
        Archive archive,
        Aws aws
) {
    public record Kafka(String topic) {
    }

    public record Archive(String bucket) {
    }

    public record Aws(String endpoint, String region) {
    }
}
