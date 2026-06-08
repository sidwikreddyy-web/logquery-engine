package com.sidwik.logquery.logs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "log_terms")
public class LogTerm {
    @Id
    private UUID id;

    @Column(name = "log_id", nullable = false)
    private UUID logId;

    @Column(nullable = false)
    private String term;

    protected LogTerm() {
    }

    public LogTerm(UUID id, UUID logId, String term) {
        this.id = id;
        this.logId = logId;
        this.term = term;
    }
}
