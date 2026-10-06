package com.aurik.monitoring.service;

import com.aurik.monitoring.domain.*;
import com.aurik.monitoring.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OperationalStateServiceTest {
    @Test
    void highestSeverityWinsEvenWhenLowerSeverityArrivesLater() {
        var events = Mockito.mock(CanonicalEventRepository.class);
        var states = Mockito.mock(MachineOperationalStateRepository.class);
        var machines = Mockito.mock(MachineReferenceRepository.class);
        var critical = new CanonicalEvent(Vendor.THERMEXWATCH, "TW-1", "EQ-001", "PLANT_01", "LINE-A", Instant.parse("2026-04-18T08:20:00Z"), "TEMPERATURE_CRITICAL", Severity.CRITICAL, 1.0, 96.0, 30.0, null, null, .95, "{}");
        var recovery = new CanonicalEvent(Vendor.PULSEFORGE, "PF-2", "EQ-001", "PLANT_01", "LINE-A", Instant.parse("2026-04-18T08:30:00Z"), "RECOVERY_SIGNAL", Severity.LOW, 3.0, 68.0, 30.0, "running", .95, .92, "{}");
        Mockito.when(events.findByMachineIdOrderByEventTimeDesc("EQ-001")).thenReturn(List.of(recovery, critical));
        Mockito.when(machines.findById("EQ-001")).thenReturn(Optional.of(new MachineReference("EQ-001", "PLANT_01", "LINE-A", "Press", "high", 85.0, 9.0, 36.0, "active")));
        Mockito.when(states.findById("EQ-001")).thenReturn(Optional.empty());
        var service = new OperationalStateService(events, states, machines, 30);
        service.recompute("EQ-001");
        var captor = org.mockito.ArgumentCaptor.forClass(MachineOperationalState.class);
        Mockito.verify(states).save(captor.capture());
        assertEquals(Severity.CRITICAL, captor.getValue().getAttentionLevel());
        assertTrue(captor.getValue().isAttentionRequired());
        assertEquals("TEMPERATURE_CRITICAL", captor.getValue().getReasonCodes());
    }
}
