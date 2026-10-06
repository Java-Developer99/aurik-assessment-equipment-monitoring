package com.aurik.monitoring.service;

import com.aurik.monitoring.domain.*;
import com.aurik.monitoring.repository.*;
import com.aurik.monitoring.service.normalizer.*;
import com.fasterxml.jackson.databind.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class IngestionProcessingService {
    private final IngestionRecordRepository ingestions;
    private final CanonicalEventRepository events;
    private final List<VendorNormalizer> normalizers;
    private final OperationalStateService stateService;
    private final ObjectMapper mapper;

    public IngestionProcessingService(IngestionRecordRepository i, CanonicalEventRepository e, List<VendorNormalizer> n, OperationalStateService s, ObjectMapper m) {
        ingestions = i;
        events = e;
        normalizers = n;
        stateService = s;
        mapper = m;
    }

    @Async("processingExecutor")
    @Transactional
    public void process(UUID id) {
        IngestionRecord in = ingestions.findById(id).orElseThrow();
        in.setStatus(ProcessingStatus.PROCESSING);
        try {
            JsonNode root = mapper.readTree(in.getRawPayload());
            VendorNormalizer normalizer = normalizers.stream().filter(n -> n.vendor() == in.getVendor()).findFirst().orElseThrow();
            List<NormalizedEvent> normalized = normalizer.normalize(root);
            Set<String> machines = new HashSet<>();
            for (NormalizedEvent n : normalized) {
                if (events.existsByVendorAndSourceRecordId(n.vendor(), n.sourceRecordId()) || events.existsByVendorAndMachineIdAndEventTimeAndEventType(n.vendor(), n.machineId(), n.eventTime(), n.eventType())) {
                    in.duplicate();
                    continue;
                }
                events.save(new CanonicalEvent(n.vendor(), n.sourceRecordId(), n.machineId(), n.plantId(), n.lineId(), n.eventTime(), n.eventType(), n.severity(), n.vibrationMmS(), n.temperatureC(), n.powerKw(), n.machineState(), n.sensorHealth(), n.confidence(), n.rawRecord()));
                in.processed();
                machines.add(n.machineId());
            }
            in.setStatus(in.getRecordsFailed() > 0 ? ProcessingStatus.PARTIAL : ProcessingStatus.PROCESSED);
            in.setProcessedAt(java.time.Instant.now());
            ingestions.save(in);
            machines.forEach(stateService::recompute);
        } catch (Exception ex) {
            in.failed();
            in.setStatus(ProcessingStatus.FAILED);
            in.setErrorMessage(ex.getMessage());
            in.setProcessedAt(java.time.Instant.now());
            ingestions.save(in);
        }
    }
}
