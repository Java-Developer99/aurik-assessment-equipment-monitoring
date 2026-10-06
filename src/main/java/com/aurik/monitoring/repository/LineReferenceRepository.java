package com.aurik.monitoring.repository;

import com.aurik.monitoring.domain.LineReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface LineReferenceRepository extends JpaRepository<LineReference, UUID> {
    Optional<LineReference> findByPlantIdAndLineId(String plantId, String lineId);
}
