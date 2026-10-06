package com.aurik.monitoring.dto;

import java.time.Instant;
import java.util.*;

public record MachineOperationalResponse(String machineId, String plantId, String lineId, String derivedStatus,
                                         String attentionLevel, boolean attentionRequired, List<String> reasonCodes,
                                         Instant latestRelevantEventTime, String processingStatus,
                                         List<String> sourceEventRefs, Instant lastProcessedAt, boolean stale) {
}
