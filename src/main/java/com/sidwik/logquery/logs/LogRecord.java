package com.sidwik.logquery.logs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_records")
public class LogRecord {
    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String message;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(nullable = false)
    private boolean archived;

    protected LogRecord() {
    }

    public LogRecord(UUID id, String eventId, String serviceName, Severity severity,
                     String message, Instant eventTimestamp, Instant receivedAt) {
        this.id = id;
        this.eventId = eventId;
        this.serviceName = serviceName;
        this.severity = severity;
        this.message = message;
        this.eventTimestamp = eventTimestamp;
        this.receivedAt = receivedAt;
        this.archived = false;
    }

    public UUID id() {
        return id;
    }

    public String eventId() {
        return eventId;
    }

    public String serviceName() {
        return serviceName;
    }

    public Severity severity() {
        return severity;
    }

    public String message() {
        return message;
    }

    public Instant eventTimestamp() {
        return eventTimestamp;
    }

    public Instant receivedAt() {
        return receivedAt;
    }

    public boolean archived() {
        return archived;
    }

    public void markArchived() {
        this.archived = true;
    }
}
