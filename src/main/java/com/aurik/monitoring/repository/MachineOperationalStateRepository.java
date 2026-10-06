package com.aurik.monitoring.repository;

import com.aurik.monitoring.domain.MachineOperationalState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface MachineOperationalStateRepository extends JpaRepository<MachineOperationalState, String> {
    List<MachineOperationalState> findByPlantId(String plantId);
}
