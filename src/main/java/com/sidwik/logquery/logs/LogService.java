package com.sidwik.logquery.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidwik.logquery.config.LogQueryProperties;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LogService {
    private final LogRecordRepository logRecordRepository;
    private final LogTermRepository logTermRepository;
    private final KeywordTokenizer keywordTokenizer;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final S3Client s3Client;
    private final LogQueryProperties properties;
    private final Clock clock;

    public LogService(
            LogRecordRepository logRecordRepository,
            LogTermRepository logTermRepository,
            KeywordTokenizer keywordTokenizer,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            S3Client s3Client,
            LogQueryProperties properties
    ) {
        this.logRecordRepository = logRecordRepository;
        this.logTermRepository = logTermRepository;
        this.keywordTokenizer = keywordTokenizer;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.s3Client = s3Client;
        this.properties = properties;
        this.clock = Clock.systemUTC();
    }

    public void publish(IngestLogRequest request) {
        try {
            kafkaTemplate.send(properties.kafka().topic(), request.serviceName(), objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Could not serialize log event", exception);
        }
    }

    @Transactional
    public LogResponse ingest(IngestLogRequest request) {
        return logRecordRepository.findByEventId(request.eventId())
                .map(LogResponse::from)
                .orElseGet(() -> persist(request));
    }

    private LogResponse persist(IngestLogRequest request) {
        Instant now = Instant.now(clock);
        LogRecord record = new LogRecord(
                UUID.randomUUID(),
                request.eventId(),
                request.serviceName(),
                request.severity(),
                request.message(),
                request.eventTimestamp(),
                now
        );
        LogRecord saved = logRecordRepository.save(record);

        Set<String> terms = keywordTokenizer.tokenize(request.message());
        terms.stream()
                .map(term -> new LogTerm(UUID.randomUUID(), saved.id(), term))
                .forEach(logTermRepository::save);

        return LogResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<LogResponse> search(SearchLogsRequest request) {
        int page = request.page() == null ? 0 : request.page();
        int size = request.size() == null ? 20 : Math.min(request.size(), 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventTimestamp"));

        return logRecordRepository.findAll(specification(request), pageable)
                .map(LogResponse::from);
    }

    @Transactional
    public ArchiveResponse archiveBefore(ArchiveRequest request) {
        List<LogRecord> records = logRecordRepository
                .findByEventTimestampBeforeAndArchivedFalseOrderByEventTimestampAsc(request.before());
        String objectKey = "archives/logs-before-" + DateTimeFormatter.ISO_INSTANT.format(request.before()) + ".jsonl";

        StringBuilder body = new StringBuilder();
        for (LogRecord record : records) {
            body.append(toArchiveLine(record)).append("\n");
            record.markArchived();
        }

        if (!records.isEmpty()) {
            putArchiveObject(objectKey, body.toString());
        }

        return new ArchiveResponse(records.size(), objectKey);
    }

    private Specification<LogRecord> specification(SearchLogsRequest request) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();

            if (request.serviceName() != null && !request.serviceName().isBlank()) {
                predicate = builder.and(predicate, builder.equal(root.get("serviceName"), request.serviceName()));
            }
            if (request.severity() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("severity"), request.severity()));
            }
            if (request.from() != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("eventTimestamp"), request.from()));
            }
            if (request.to() != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("eventTimestamp"), request.to()));
            }
            if (request.keyword() != null && !request.keyword().isBlank()) {
                String keyword = request.keyword().toLowerCase();
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<LogTerm> termRoot = subquery.from(LogTerm.class);
                subquery.select(termRoot.get("logId"))
                        .where(builder.equal(termRoot.get("term"), keyword));
                predicate = builder.and(predicate, root.get("id").in(subquery));
            }

            return predicate;
        };
    }

    private String toArchiveLine(LogRecord record) {
        try {
            return objectMapper.writeValueAsString(LogResponse.from(record));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize archive line", exception);
        }
    }

    private void putArchiveObject(String objectKey, String body) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(properties.archive().bucket())
                    .key(objectKey)
                    .build(), RequestBody.fromString(body));
        } catch (NoSuchBucketException exception) {
            s3Client.createBucket(builder -> builder.bucket(properties.archive().bucket()));
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(properties.archive().bucket())
                    .key(objectKey)
                    .build(), RequestBody.fromString(body));
        }
    }
}
