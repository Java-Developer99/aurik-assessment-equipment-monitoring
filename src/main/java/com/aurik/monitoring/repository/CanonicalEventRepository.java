package com.aurik.monitoring.repository;

import com.aurik.monitoring.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.*;

public interface CanonicalEventRepository extends JpaRepository<CanonicalEvent, UUID> {
    boolean existsByVendorAndSourceRecordId(Vendor vendor, String sourceRecordId);

    boolean existsByVendorAndMachineIdAndEventTimeAndEventType(Vendor vendor, String machineId, Instant eventTime, String eventType);

    List<CanonicalEvent> findByMachineIdOrderByEventTimeDesc(String machineId);

    List<CanonicalEvent> findByPlantId(String plantId);
}
