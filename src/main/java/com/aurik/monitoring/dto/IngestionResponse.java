package com.aurik.monitoring.dto;

import java.util.UUID;

public record IngestionResponse(UUID ingestionId, String status, String message) {
}
