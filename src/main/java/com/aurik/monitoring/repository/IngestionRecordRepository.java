package com.aurik.monitoring.repository;

import com.aurik.monitoring.domain.IngestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngestionRecordRepository extends JpaRepository<IngestionRecord, UUID> {
}
