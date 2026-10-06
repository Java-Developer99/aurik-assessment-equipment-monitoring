package com.aurik.monitoring.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "machine_operational_state")
public class   MachineOperationalState {
    @Id
    private String machineId;

    @Column(nullable = false)
    private String plantId, lineId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DerivedStatus derivedStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity attentionLevel;

    @Column(nullable = false)
    private boolean attentionRequired;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reasonCodes;

    private Instant latestRelevantEventTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus processingStatus;

    private Instant lastProcessedAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceEventRefs;

    protected MachineOperationalState() {
    }

    public MachineOperationalState(String m, String p, String l, DerivedStatus d, Severity s, boolean a, String r, Instant latest, ProcessingStatus ps, String refs, Instant lp) {
        machineId = m;
        plantId = p;
        lineId = l;
        derivedStatus = d;
        attentionLevel = s;
        attentionRequired = a;
        reasonCodes = r;
        latestRelevantEventTime = latest;
        processingStatus = ps;
        sourceEventRefs = refs;
        lastProcessedAt = lp;
    }

    public void update(DerivedStatus d, Severity s, boolean a, String r, Instant latest, ProcessingStatus ps, String refs, Instant lp) {
        derivedStatus = d;
        attentionLevel = s;
        attentionRequired = a;
        reasonCodes = r;
        latestRelevantEventTime = latest;
        processingStatus = ps;
        sourceEventRefs = refs;
        lastProcessedAt = lp;
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

    public DerivedStatus getDerivedStatus() {
        return derivedStatus;
    }

    public Severity getAttentionLevel() {
        return attentionLevel;
    }

    public boolean isAttentionRequired() {
        return attentionRequired;
    }

    public String getReasonCodes() {
        return reasonCodes;
    }

    public Instant getLatestRelevantEventTime() {
        return latestRelevantEventTime;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public Instant getLastProcessedAt() {
        return lastProcessedAt;
    }

    public String getSourceEventRefs() {
        return sourceEventRefs;
    }
}
