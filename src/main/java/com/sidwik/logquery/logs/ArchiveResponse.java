package com.sidwik.logquery.logs;

public record ArchiveResponse(
        int archivedRecords,
        String objectKey
) {
}
