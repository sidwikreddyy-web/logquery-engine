package com.sidwik.logquery.logs;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ArchiveRequest(
        @NotNull Instant before
) {
}
