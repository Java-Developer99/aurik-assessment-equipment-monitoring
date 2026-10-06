package com.aurik.monitoring.dto;

import java.time.Instant;
import java.util.UUID;

public record IngestionStatusResponse(UUID ingestionId, String vendor, String status, Instant receivedAt,
                                      Instant processedAt, int recordsReceived, int recordsProcessed,
                                      int recordsDuplicate, int recordsFailed, String errorMessage) {
}
