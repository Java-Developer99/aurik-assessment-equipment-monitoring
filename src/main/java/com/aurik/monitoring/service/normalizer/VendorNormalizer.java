package com.aurik.monitoring.service.normalizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.aurik.monitoring.domain.Vendor;

import java.util.List;

public interface VendorNormalizer {
    Vendor vendor();

    List<NormalizedEvent> normalize(JsonNode payload);
}
