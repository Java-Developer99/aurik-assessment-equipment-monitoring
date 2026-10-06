package com.aurik.monitoring.service;

import com.aurik.monitoring.domain.*;
import com.aurik.monitoring.repository.IngestionRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IngestionService {
    private final IngestionRecordRepository repo;
    private final IngestionProcessingService processor;
    private final ObjectMapper mapper;

    public IngestionService(IngestionRecordRepository repo, IngestionProcessingService processor, ObjectMapper mapper) {
        this.repo = repo;
        this.processor = processor;
        this.mapper = mapper;
    }

    public UUID accept(Vendor vendor, JsonNode payload) {
        if (payload == null || payload.isNull() || !payload.isObject())
            throw new IllegalArgumentException("JSON object payload is required");
        int count = count(payload, vendor);
        if (count < 1) throw new IllegalArgumentException("Vendor payload contains no records");
        try {
            IngestionRecord saved = repo.save(new IngestionRecord(vendor, mapper.writeValueAsString(payload), count));
            processor.process(saved.getId());
            return saved.getId();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON payload: " + e.getMessage(), e);
        }
    }

    private int count(JsonNode p, Vendor v) {
        return switch (v) {
            case PULSEFORGE -> p.path("events").size();
            case THERMEXWATCH -> p.path("readings").size();
            case MAINTAFLOW -> p.path("records").size();
        };
    }
}
