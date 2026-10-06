package com.aurik.monitoring.service.normalizer;

import com.aurik.monitoring.domain.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class ThermexWatchNormalizer implements VendorNormalizer {
    private final ReferenceResolver refs;

    public ThermexWatchNormalizer(ReferenceResolver refs) {
        this.refs = refs;
    }

    public Vendor vendor() {
        return Vendor.THERMEXWATCH;
    }

    public List<NormalizedEvent> normalize(JsonNode root) {
        List<NormalizedEvent> out = new ArrayList<>();
        String plant = text(root, "site_code");
        for (JsonNode r : root.path("readings")) {
            out.add(one(r, plant));
        }
        if (out.isEmpty()) throw new NormalizationException("readings must contain at least one record");
        return out;
    }

    private NormalizedEvent one(JsonNode r, String plant) {
        String id = text(r, "readingId");
        String machine = text(r, "assetCode");
        MachineReference m = refs.resolveMachine(plant, machine);
        String line = refs.resolveLine(text(r, "productionLine"), m);
        JsonNode ts = r.get("timestampMs");
        if (ts == null || !ts.isNumber()) throw new NormalizationException("timestampMs must be numeric");
        Instant time = Instant.ofEpochMilli(ts.asLong());
        String alert = text(r, "alertCode");
        Severity sev = severity(r.get("level"), alert);
        Double tempF = num(r, "temperature_f"), g = num(r, "vibration_g");
        return new NormalizedEvent(vendor(), id, machine, m.getPlantId(), line, time, mapAlert(alert), sev, ReferenceResolver.mmPerSecFromG(g), ReferenceResolver.celsiusFromFahrenheit(tempF), num(r, "power_kw"), null, null, signalConfidence(text(r, "signal_quality")), r.toString());
    }

    private String mapAlert(String a) {
        return switch (a) {
            case "VIB_WARN" -> "VIBRATION_WARNING";
            case "TEMP_WARN" -> "TEMPERATURE_WARNING";
            case "TEMP_CRIT" -> "TEMPERATURE_CRITICAL";
            case "POWER_DROP" -> "POWER_DROP";
            case "OK" -> "NORMAL_SIGNAL";
            default -> throw new NormalizationException("Unsupported ThermexWatch alertCode: " + a);
        };
    }

    private Severity severity(JsonNode n, String alert) {
        if (n == null || !n.isInt() || n.asInt() < 1 || n.asInt() > 5)
            throw new NormalizationException("level must be integer 1..5");
        return switch (n.asInt()) {
            case 1 -> Severity.NORMAL;
            case 2 -> Severity.LOW;
            case 3 -> Severity.MEDIUM;
            case 4 -> Severity.HIGH;
            default -> Severity.CRITICAL;
        };
    }

    private double signalConfidence(String q) {
        if (q == null) return 0.0;
        return switch (q.toUpperCase()) {
            case "GOOD" -> 0.95;
            case "FAIR" -> 0.75;
            case "POOR" -> 0.45;
            default -> 0.0;
        };
    }

    private static String text(JsonNode n, String f) {
        JsonNode x = n.get(f);
        return x == null || x.isNull() ? null : x.isTextual() ? x.asText() : x.toString();
    }

    private static Double num(JsonNode n, String f) {
        JsonNode x = n.get(f);
        if (x == null || x.isNull()) throw new NormalizationException(f + " is required");
        if (!x.isNumber()) throw new NormalizationException(f + " must be numeric");
        return x.asDouble();
    }
}
