package com.sidwik.logquery.logs;

import java.time.Instant;

public record SearchLogsRequest(
        String serviceName,
        Severity severity,
        Instant from,
        Instant to,
        String keyword,
        Integer page,
        Integer size
) {
}
