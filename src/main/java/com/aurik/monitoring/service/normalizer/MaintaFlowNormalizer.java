package com.aurik.monitoring.service.normalizer;

import com.aurik.monitoring.domain.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class MaintaFlowNormalizer implements VendorNormalizer {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private final ReferenceResolver refs;

    public MaintaFlowNormalizer(ReferenceResolver refs) {
        this.refs = refs;
    }

    public Vendor vendor() {
        return Vendor.MAINTAFLOW;
    }

    public List<NormalizedEvent> normalize(JsonNode root) {
        List<NormalizedEvent> out = new ArrayList<>();
        String plant = text(root, "factory_id");
        for (JsonNode r : root.path("records")) {
            out.add(one(r, plant));
        }
        if (out.isEmpty()) throw new NormalizationException("records must contain at least one record");
        return out;
    }

    private NormalizedEvent one(JsonNode r, String plant) {
        String id = text(r, "record_id");
        String machine = text(r, "machine_ref");
        MachineReference m = refs.resolveMachine(plant, machine);
        String line = refs.resolveLine(text(r, "line_ref"), m);
        String rawTime = text(r, "recorded_at");
        Instant time;
        try {
            time = LocalDateTime.parse(rawTime, FORMAT).toInstant(ZoneOffset.UTC);
        } catch (Exception x) {
            throw new NormalizationException("Invalid MaintaFlow recorded_at: " + rawTime);
        }
        String result = text(r, "inspection_result"), maintenance = text(r, "maintenance_status");
        String type;
        Severity sev;
        if ("major_defect_found".equals(result)) {
            type = "MAJOR_DEFECT";
            sev = Severity.HIGH;
        } else if ("minor_defect_found".equals(result)) {
            type = "MINOR_DEFECT";
            sev = Severity.MEDIUM;
        } else if ("passed_with_observation".equals(result)) {
            type = "INSPECTION_OBSERVATION";
            sev = Severity.LOW;
        } else if ("calibration_pending".equals(result)) {
            type = "CALIBRATION_PENDING";
            sev = Severity.MEDIUM;
        } else if ("overdue".equals(maintenance)) {
            type = "MAINTENANCE_OVERDUE";
            sev = Severity.HIGH;
        } else if ("due_soon".equals(maintenance)) {
            type = "MAINTENANCE_DUE_SOON";
            sev = Severity.LOW;
        } else if ("completed".equals(maintenance)) {
            type = "MAINTENANCE_COMPLETED";
            sev = Severity.NORMAL;
        } else {
            type = "OPERATOR_NOTE";
            sev = Severity.LOW;
        }
        return new NormalizedEvent(vendor(), id, machine, m.getPlantId(), line, time, type, sev, null, null, null, null, null, confidence(text(r, "manual_confidence")), r.toString());
    }

    private double confidence(String c) {
        return switch (c == null ? "" : c.toLowerCase()) {
            case "high" -> 0.95;
            case "medium" -> 0.75;
            case "low" -> 0.5;
            default -> 0.0;
        };
    }

    private static String text(JsonNode n, String f) {
        JsonNode x = n.get(f);
        return x == null || x.isNull() ? null : x.isTextual() ? x.asText() : x.toString();
    }
}
