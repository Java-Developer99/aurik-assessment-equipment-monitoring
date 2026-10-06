package com.aurik.monitoring.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "canonical_events", uniqueConstraints = @UniqueConstraint(name = "uk_vendor_source", columnNames = {"vendor", "source_record_id"}))
public class CanonicalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Vendor vendor;

    @Column(name = "source_record_id", nullable = false)
    private String sourceRecordId;

    @Column(nullable = false)
    private String machineId, plantId, lineId, eventType;

    @Column(nullable = false)
    private Instant eventTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    private Double vibrationMmS, temperatureC, powerKw, sensorHealth, confidence;

    private String machineState;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawRecord;

    protected CanonicalEvent() {
    }

    public CanonicalEvent(Vendor v, String source, String machine, String plant, String line, Instant time, String type, Severity sev, Double vib, Double temp, Double power, String state, Double health, Double conf, String raw) {
        vendor = v;
        sourceRecordId = source;
        machineId = machine;
        plantId = plant;
        lineId = line;
        eventTime = time;
        eventType = type;
        severity = sev;
        vibrationMmS = vib;
        temperatureC = temp;
        powerKw = power;
        machineState = state;
        sensorHealth = health;
        confidence = conf;
        rawRecord = raw;
    }

    public UUID getId() {
        return id;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public String getSourceRecordId() {
        return sourceRecordId;
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

    public Instant getEventTime() {
        return eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Double getVibrationMmS() {
        return vibrationMmS;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public Double getPowerKw() {
        return powerKw;
    }

    public String getMachineState() {
        return machineState;
    }

    public Double getSensorHealth() {
        return sensorHealth;
    }

    public Double getConfidence() {
        return confidence;
    }
}
