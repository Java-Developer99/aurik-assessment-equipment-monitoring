package com.aurik.monitoring.controller;

import com.aurik.monitoring.domain.*;
import com.aurik.monitoring.dto.PlantSummaryResponse;
import com.aurik.monitoring.repository.MachineOperationalStateRepository;
import com.aurik.monitoring.service.OperationalStateService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/plants")
public class PlantController {
    private final MachineOperationalStateRepository states;
    private final OperationalStateService service;

    public PlantController(MachineOperationalStateRepository s, OperationalStateService o) {
        states = s;
        service = o;
    }

    @GetMapping("/{plantId}/summary")
    public PlantSummaryResponse summary(@PathVariable String plantId) {
        List<MachineOperationalState> all = states.findByPlantId(plantId);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (DerivedStatus s : DerivedStatus.values())
            counts.put(s.name(), (int) all.stream().filter(x -> x.getDerivedStatus() == s).count());
        List<PlantSummaryResponse.LineSummary> lines = all.stream().collect(Collectors.groupingBy(MachineOperationalState::getLineId, TreeMap::new, Collectors.collectingAndThen(Collectors.toList(), xs -> new PlantSummaryResponse.LineSummary(xs.get(0).getLineId(), (int) xs.stream().filter(MachineOperationalState::isAttentionRequired).count())))).values().stream().toList();
        List<String> critical = all.stream().filter(x -> x.getAttentionLevel() == Severity.CRITICAL).map(MachineOperationalState::getMachineId).sorted().toList();
        int stale = (int) all.stream().filter(service::isStale).count();
        return new PlantSummaryResponse(plantId, counts, lines, critical, stale);
    }
}
