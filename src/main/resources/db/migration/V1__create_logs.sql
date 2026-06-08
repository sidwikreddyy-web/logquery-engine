CREATE TABLE log_records (
    id UUID PRIMARY KEY,
    event_id VARCHAR(160) NOT NULL UNIQUE,
    service_name VARCHAR(160) NOT NULL,
    severity VARCHAR(40) NOT NULL,
    message TEXT NOT NULL,
    event_timestamp TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_log_records_filter
    ON log_records (service_name, severity, event_timestamp DESC);

CREATE TABLE log_terms (
    id UUID PRIMARY KEY,
    log_id UUID NOT NULL REFERENCES log_records(id) ON DELETE CASCADE,
    term VARCHAR(120) NOT NULL
);

CREATE INDEX idx_log_terms_term
    ON log_terms (term);

CREATE INDEX idx_log_terms_log_id
    ON log_terms (log_id);
