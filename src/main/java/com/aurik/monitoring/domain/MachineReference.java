package com.aurik.monitoring.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "machine_reference")
public class MachineReference {
    @Id
    private String machineId;

    @Column(nullable = false)
    private String plantId, lineId;

    private String machineType, criticality, assetStatus;

    private Double ratedMaxTempC, ratedMaxVibrationMmS, baselinePowerKw;

    protected MachineReference() {
    }

    public MachineReference(String id, String plant, String line, String type, String crit, Double temp, Double vib, Double power, String status) {
        machineId = id;
        plantId = plant;
        lineId = line;
        machineType = type;
        criticality = crit;
        ratedMaxTempC = temp;
        ratedMaxVibrationMmS = vib;
        baselinePowerKw = power;
        assetStatus = status;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getPlantId() {
        return plantId;
    }

    public String getLineId() {
        return lineId;
    }

    public String getAssetStatus() {
        return assetStatus;
    }
}
