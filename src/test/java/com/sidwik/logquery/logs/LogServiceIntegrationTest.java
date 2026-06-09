package com.sidwik.logquery.logs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false",
        "logquery.aws.endpoint=http://localhost:4567"
})
@Testcontainers(disabledWithoutDocker = true)
class LogServiceIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private LogService logService;

    @Autowired
    private LogRecordRepository logRecordRepository;

    @Autowired
    private LogTermRepository logTermRepository;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void ingestIsIdempotentByEventId() {
        IngestLogRequest request = sampleRequest("evt-duplicate", "checkout payment timeout");

        LogResponse first = logService.ingest(request);
        LogResponse second = logService.ingest(request);

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(logRecordRepository.count()).isEqualTo(1);
        assertThat(logTermRepository.count()).isEqualTo(3);
    }

    @Test
    void searchFindsLogsByIndexedKeyword() {
        logService.ingest(sampleRequest("evt-search-1", "checkout latency crossed threshold"));
        logService.ingest(sampleRequest("evt-search-2", "profile updated successfully"));

        Page<LogResponse> results = logService.search(new SearchLogsRequest(
                "checkout-api",
                Severity.ERROR,
                null,
                null,
                "latency",
                0,
                10
        ));

        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().get(0).eventId()).isEqualTo("evt-search-1");
    }

    private IngestLogRequest sampleRequest(String eventId, String message) {
        return new IngestLogRequest(
                eventId,
                "checkout-api",
                Severity.ERROR,
                message,
                Instant.parse("2025-08-15T10:15:30Z")
        );
    }
}
