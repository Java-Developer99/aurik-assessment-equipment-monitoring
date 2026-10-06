package com.aurik.monitoring.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ingestion_records")
public class IngestionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Vendor vendor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status;

    @Column(nullable = false)
    private Instant receivedAt;

    private Instant processedAt;

    @Column(nullable = false)
    private int recordsReceived, recordsProcessed, recordsDuplicate, recordsFailed;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    protected IngestionRecord() {
    }

    public IngestionRecord(Vendor v, String raw, int count) {
        vendor = v;
        rawPayload = raw;
        recordsReceived = count;
        status = ProcessingStatus.ACCEPTED;
        receivedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus s) {
        status = s;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant x) {
        processedAt = x;
    }

    public int getRecordsReceived() {
        return recordsReceived;
    }

    public int getRecordsProcessed() {
        return recordsProcessed;
    }

    public int getRecordsDuplicate() {
        return recordsDuplicate;
    }

    public int getRecordsFailed() {
        return recordsFailed;
    }

    public void processed() {
        recordsProcessed++;
    }

    public void duplicate() {
        recordsDuplicate++;
    }

    public void failed() {
        recordsFailed++;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String x) {
        errorMessage = x;
    }
}
