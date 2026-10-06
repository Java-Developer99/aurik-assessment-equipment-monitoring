package com.aurik.monitoring.service.normalizer;

import com.aurik.monitoring.domain.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class PulseForgeNormalizer implements VendorNormalizer {
    private final ObjectMapper mapper;
    private final ReferenceResolver refs;

    public PulseForgeNormalizer(ObjectMapper mapper, ReferenceResolver refs) {
        this.mapper = mapper;
        this.refs = refs;
    }

    public Vendor vendor() {
        return Vendor.PULSEFORGE;
    }

    public List<NormalizedEvent> normalize(JsonNode root) {
        List<NormalizedEvent> out = new ArrayList<>();
        String plant = text(root, "plant_id");
        for (JsonNode e : root.path("events")) {
            out.add(one(e, plant));
        }
        if (out.isEmpty()) throw new NormalizationException("events must contain at least one record");
        return out;
    }

    private NormalizedEvent one(JsonNode e, String plant) {
        String id = text(e, "event_id");
        if (id == null) throw new NormalizationException("event_id is required");
        String machine = text(e, "machine_id");
        MachineReference m = refs.resolveMachine(plant, machine);
        String line = refs.resolveLine(text(e, "line_id"), m);
        String ts = text(e, "event_time");
        Instant time;
        try {
            time = Instant.parse(ts);
        } catch (Exception x) {
            throw new NormalizationException("Invalid PulseForge event_time: " + ts);
        }
        String type = text(e, "event_type");
        Severity sev = severity(text(e, "severity"));
        Double temp = number(e, "temperature_c"), vib = number(e, "vibration_mm_s"), health = number(e, "sensor_health"), conf = number(e, "vendor_confidence");
        validateRange(temp, -50, 200, "temperature_c");
        validateRange(health, 0, 1, "sensor_health");
        validateRange(conf, 0, 1, "vendor_confidence");
        return new NormalizedEvent(vendor(), id, machine, m.getPlantId(), line, time, type, sev, vib, temp, number(e, "power_kw"), text(e, "machine_state"), health, conf, e.toString());
    }

    private Severity severity(String s) {
        if (s == null) throw new NormalizationException("severity is required");
        try {
            return Severity.valueOf(s.trim().toUpperCase());
        } catch (Exception x) {
            throw new NormalizationException("Unsupported PulseForge severity: " + s);
        }
    }

    private static String text(JsonNode n, String f) {
        JsonNode x = n.get(f);
        return x == null || x.isNull() ? null : x.isTextual() ? x.asText() : x.toString();
    }

    private static Double number(JsonNode n, String f) {
        JsonNode x = n.get(f);
        if (x == null || x.isNull()) return null;
        if (!x.isNumber()) throw new NormalizationException(f + " must be numeric");
        return x.asDouble();
    }

    private static void validateRange(Double x, double min, double max, String f) {
        if (x != null && (x < min || x > max)) throw new NormalizationException(f + " outside accepted range");
    }
}
