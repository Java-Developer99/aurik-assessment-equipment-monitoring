package com.aurik.monitoring.service.normalizer;

import com.aurik.monitoring.domain.*;

import java.time.Instant;

public record NormalizedEvent(Vendor vendor, String sourceRecordId, String machineId, String plantId, String lineId,
                              Instant eventTime, String eventType, Severity severity, Double vibrationMmS,
                              Double temperatureC, Double powerKw, String machineState, Double sensorHealth,
                              Double confidence, String rawRecord) {
}
