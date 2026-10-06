package com.aurik.monitoring.service.normalizer;

import com.aurik.monitoring.domain.*;
import com.aurik.monitoring.repository.MachineReferenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ThermexWatchNormalizerTest {
    @Test
    void normalizesEpochAndUnits() {
        var repo = Mockito.mock(MachineReferenceRepository.class);
        Mockito.when(repo.findByMachineIdAndPlantId("EQ-001", "PLANT_01")).thenReturn(Optional.of(new MachineReference("EQ-001", "PLANT_01", "LINE-A", "Press", "high", 85.0, 9.0, 36.0, "active")));
        var mapper = new ObjectMapper();
        var n = new ThermexWatchNormalizer(new ReferenceResolver(repo));
        var root = mapper.createObjectNode();
        root.put("site_code", "PLANT_01");
        var r = root.putArray("readings").addObject();
        r.put("readingId", "TW-X");
        r.put("assetCode", "EQ-001");
        r.put("productionLine", "A");
        r.put("timestampMs", 1776499152000L);
        r.put("alertCode", "VIB_WARN");
        r.put("level", 4);
        r.put("vibration_g", 0.81);
        r.put("temperature_f", 181.2);
        r.put("power_kw", 37.8);
        var x = n.normalize(root).get(0);
        assertEquals(Severity.HIGH, x.severity());
        assertEquals("LINE-A", x.lineId());
        assertEquals(82.88888888888889, x.temperatureC(), 1e-9);
        assertEquals(7.9433865, x.vibrationMmS(), 1e-7);
        assertEquals("VIBRATION_WARNING", x.eventType());
    }

    @Test
    void rejectsUnknownAlert() {
        var repo = Mockito.mock(MachineReferenceRepository.class);
        Mockito.when(repo.findByMachineIdAndPlantId("EQ-001", "PLANT_01")).thenReturn(Optional.of(new MachineReference("EQ-001", "PLANT_01", "LINE-A", "Press", "high", 85.0, 9.0, 36.0, "active")));
        var mapper = new ObjectMapper();
        var n = new ThermexWatchNormalizer(new ReferenceResolver(repo));
        var root = mapper.createObjectNode();
        root.put("site_code", "PLANT_01");
        var r = root.putArray("readings").addObject();
        r.put("readingId", "TW-X");
        r.put("assetCode", "EQ-001");
        r.put("productionLine", "A");
        r.put("timestampMs", 1776499152000L);
        r.put("alertCode", "UNKNOWN_ALERT");
        r.put("level", 4);
        r.put("vibration_g", 0.2);
        r.put("temperature_f", 180);
        r.put("power_kw", 30);
        assertThrows(NormalizationException.class, () -> n.normalize(root));
    }
}
