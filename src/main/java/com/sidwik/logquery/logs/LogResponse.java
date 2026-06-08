package com.sidwik.logquery.logs;

import java.time.Instant;
import java.util.UUID;

public record LogResponse(
        UUID id,
        String eventId,
        String serviceName,
        Severity severity,
        String message,
        Instant eventTimestamp,
        Instant receivedAt,
        boolean archived
) {
    public static LogResponse from(LogRecord record) {
        return new LogResponse(
                record.id(),
                record.eventId(),
                record.serviceName(),
                record.severity(),
                record.message(),
                record.eventTimestamp(),
                record.receivedAt(),
                record.archived()
        );
    }
}
