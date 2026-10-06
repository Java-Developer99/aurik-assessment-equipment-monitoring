package com.aurik.monitoring.service;

import com.aurik.monitoring.domain.*;
import com.aurik.monitoring.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class OperationalStateService {
    private final CanonicalEventRepository events;
    private final MachineOperationalStateRepository states;
    private final MachineReferenceRepository machines;
    private final int staleMinutes;

    public OperationalStateService(CanonicalEventRepository events, MachineOperationalStateRepository states, MachineReferenceRepository machines, @Value("${app.processing.stale-minutes:30}") int staleMinutes) {
        this.events = events;
        this.states = states;
        this.machines = machines;
        this.staleMinutes = staleMinutes;
    }

    @Transactional
    public void recompute(String machineId) {

        List<CanonicalEvent> all =
                events.findByMachineIdOrderByEventTimeDesc(machineId);

        if (all.isEmpty()) {
            return;
        }

        Instant processed = Instant.now();

        Map<Vendor, CanonicalEvent> latestByVendor =
                new EnumMap<>(Vendor.class);

        for (CanonicalEvent event : all) {
            latestByVendor.putIfAbsent(event.getVendor(), event);
        }

        CanonicalEvent newest = all.get(0);

        Instant staleCutoff =
                processed.minusSeconds(staleMinutes * 60L);

        List<CanonicalEvent> activeEvents =
                latestByVendor.values().stream()
                        .filter(event ->
                                !event.getEventTime().isBefore(staleCutoff))
                        .toList();

        Severity highest =
                activeEvents.stream()
                        .map(CanonicalEvent::getSeverity)
                        .max(Comparator.comparingInt(Severity::rank))
                        .orElse(Severity.NORMAL);

        boolean attention =
                highest.rank() > Severity.NORMAL.rank();

        Set<String> reasons = new LinkedHashSet<>();

        for (CanonicalEvent event : activeEvents) {
            if (event.getSeverity() == highest
                    && event.getSeverity() != Severity.NORMAL) {

                reasons.add(event.getEventType());
            }
        }

        if (reasons.isEmpty()) {
            reasons.add("NO_ACTIVE_ALERT");
        }

        ProcessingStatus ps = ProcessingStatus.PROCESSED;

        String refs =
                activeEvents.stream()
                        .filter(event -> event.getSeverity() == highest)
                        .map(CanonicalEvent::getSourceRecordId)
                        .distinct()
                        .limit(10)
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");

        MachineReference machine =
                machines.findById(machineId)
                        .orElseThrow();

        MachineOperationalState state =
                states.findById(machineId)
                        .orElseGet(() ->
                                new MachineOperationalState(
                                        machineId,
                                        machine.getPlantId(),
                                        machine.getLineId(),
                                        DerivedStatus.NORMAL,
                                        Severity.NORMAL,
                                        false,
                                        "",
                                        newest.getEventTime(),
                                        ps,
                                        refs,
                                        processed
                                ));

        state.update(
                attention
                        ? DerivedStatus.ATTENTION_REQUIRED
                        : DerivedStatus.NORMAL,
                highest,
                attention,
                String.join(",", reasons),
                newest.getEventTime(),
                ps,
                refs,
                processed
        );

        states.save(state);
    }


    public boolean isStale(MachineOperationalState state) {
        if (state.getLatestRelevantEventTime() == null) {
            return true;
        }
        Instant now = Instant.now();
        return state.getLatestRelevantEventTime()
                .isBefore(now.minusSeconds(staleMinutes * 60L));
    }
}
