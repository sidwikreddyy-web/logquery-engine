package com.sidwik.logquery.logs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record IngestLogRequest(
        @NotBlank String eventId,
        @NotBlank String serviceName,
        @NotNull Severity severity,
        @NotBlank String message,
        @NotNull Instant eventTimestamp
) {
}
