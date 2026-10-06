package com.aurik.monitoring.repository;

import com.aurik.monitoring.domain.MachineReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface MachineReferenceRepository extends JpaRepository<MachineReference, String> {
    Optional<MachineReference> findByMachineIdAndPlantId(String machineId, String plantId);
}
