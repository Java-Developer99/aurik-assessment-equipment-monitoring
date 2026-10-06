package com.aurik.monitoring.dto;

import java.util.*;

public record PlantSummaryResponse(String plantId, Map<String, Integer> statusCounts, List<LineSummary> lines,
                                   List<String> criticalMachines, int staleMachines) {
    public record LineSummary(String lineId, int machinesRequiringAttention) {
    }
}
