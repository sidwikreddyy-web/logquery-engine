package com.sidwik.logquery.logs;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogTermRepository extends JpaRepository<LogTerm, UUID> {
}
