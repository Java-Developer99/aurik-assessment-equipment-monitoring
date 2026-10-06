package com.aurik.monitoring.service.normalizer;

import com.aurik.monitoring.domain.*;
import com.aurik.monitoring.repository.MachineReferenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PulseForgeNormalizerTest {
    @Test
    void normalizesPulseForgeAndResolvesReferenceLine() {
        MachineReferenceRepository repo = Mockito.mock(MachineReferenceRepository.class);
        Mockito.when(repo.findByMachineIdAndPlantId("EQ-001", "PLANT_01")).thenReturn(Optional.of(new MachineReference("EQ-001", "PLANT_01", "LINE-A", "Press", "high", 85.0, 9.0, 36.0, "active")));
        var normalizer = new PulseForgeNormalizer(new ObjectMapper(), new ReferenceResolver(repo));
        var root = new ObjectMapper().createObjectNode();
        root.put("vendor", "PulseForge");
        root.put("plant_id", "PLANT_01");
        var events = root.putArray("events");
        var e = events.addObject();
        e.put("event_id", "PF-1");
        e.put("machine_id", "EQ-001");
        e.put("line_id", "LINE-A");
        e.put("event_time", "2026-04-18T07:59:12Z");
        e.put("event_type", "HIGH_VIBRATION");
        e.put("severity", "high");
        e.put("vibration_mm_s", 11.8);
        e.put("temperature_c", 83.2);
        e.put("sensor_health", 0.91);
        e.put("vendor_confidence", 0.87);
        var result = normalizer.normalize(root).get(0);
        assertEquals("EQ-001", result.machineId());
        assertEquals("LINE-A", result.lineId());
        assertEquals(Severity.HIGH, result.severity());
        assertEquals(11.8, result.vibrationMmS());
    }

    @Test
    void rejectsInvalidSeverity() {
        MachineReferenceRepository repo = Mockito.mock(MachineReferenceRepository.class);
        Mockito.when(repo.findByMachineIdAndPlantId("EQ-001", "PLANT_01")).thenReturn(Optional.of(new MachineReference("EQ-001", "PLANT_01", "LINE-A", "Press", "high", 85.0, 9.0, 36.0, "active")));
        var normalizer = new PulseForgeNormalizer(new ObjectMapper(), new ReferenceResolver(repo));
        var root = new ObjectMapper().createObjectNode();
        root.put("plant_id", "PLANT_01");
        var e = root.putArray("events").addObject();
        e.put("event_id", "PF-X");
        e.put("machine_id", "EQ-001");
        e.put("line_id", "LINE-A");
        e.put("event_time", "2026-04-18T07:59:12Z");
        e.put("event_type", "TEMP_SPIKE");
        e.put("severity", "urgent");
        assertThrows(NormalizationException.class, () -> normalizer.normalize(root));
    }
}
