package com.sidwik.logquery.logs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LogRecordRepository extends JpaRepository<LogRecord, UUID>, JpaSpecificationExecutor<LogRecord> {
    Optional<LogRecord> findByEventId(String eventId);

    List<LogRecord> findByEventTimestampBeforeAndArchivedFalseOrderByEventTimestampAsc(Instant before);
}
