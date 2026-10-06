package com.aurik.monitoring.controller;

import com.aurik.monitoring.domain.MachineOperationalState;
import com.aurik.monitoring.dto.MachineOperationalResponse;
import com.aurik.monitoring.repository.MachineOperationalStateRepository;
import com.aurik.monitoring.service.OperationalStateService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/machines")
public class MachineController {
    private final MachineOperationalStateRepository repo;
    private final OperationalStateService service;

    public MachineController(MachineOperationalStateRepository r, OperationalStateService s) {
        repo = r;
        service = s;
    }

    @GetMapping("/{machineId}/operational-view")
    public MachineOperationalResponse view(@PathVariable String machineId) {
        MachineOperationalState x = repo.findById(machineId).orElseThrow(() -> new NoSuchElementException("No processed state for machine: " + machineId));
        List<String> reasons = x.getReasonCodes().isBlank() ? List.of() : List.of(x.getReasonCodes().split(","));
        List<String> refs = x.getSourceEventRefs().isBlank() ? List.of() : List.of(x.getSourceEventRefs().split(","));
        return new MachineOperationalResponse(x.getMachineId(), x.getPlantId(), x.getLineId(), x.getDerivedStatus().name(), x.getAttentionLevel().name(), x.isAttentionRequired(), reasons, x.getLatestRelevantEventTime(), x.getProcessingStatus().name(), refs, x.getLastProcessedAt(), service.isStale(x));
    }
}
