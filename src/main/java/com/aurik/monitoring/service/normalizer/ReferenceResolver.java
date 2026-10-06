package com.aurik.monitoring.service.normalizer;

import com.aurik.monitoring.domain.MachineReference;
import com.aurik.monitoring.repository.MachineReferenceRepository;
import org.springframework.stereotype.Component;

@Component
public class ReferenceResolver {
    private final MachineReferenceRepository machines;

    public ReferenceResolver(MachineReferenceRepository machines) {
        this.machines = machines;
    }

    public MachineReference resolveMachine(String plantId, String machineId) {
        if (plantId == null || plantId.isBlank() || machineId == null || machineId.isBlank())
            throw new NormalizationException("machine_id and plant_id are required");
        return machines.findByMachineIdAndPlantId(machineId, plantId).orElseThrow(() -> new NormalizationException("Unknown machine: " + machineId + " in plant " + plantId));
    }

    public String resolveLine(String supplied, MachineReference machine) {
        if (supplied == null || supplied.isBlank()) throw new NormalizationException("line is required");
        String candidate = supplied.startsWith("LINE-") ? supplied : "LINE-" + supplied;
        if (!machine.getLineId().equals(candidate))
            throw new NormalizationException("Line " + supplied + " does not match machine " + machine.getMachineId() + " (expected " + machine.getLineId() + ")");
        return machine.getLineId();
    }

    public static double celsiusFromFahrenheit(double f) {
        return (f - 32.0) * 5.0 / 9.0;
    }

    public static double mmPerSecFromG(double g) {
        return g * 9.80665 * 1000.0 / 1000.0;
    }
}
