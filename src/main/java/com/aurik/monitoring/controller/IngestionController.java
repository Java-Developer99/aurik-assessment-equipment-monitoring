package com.aurik.monitoring.controller;

import com.aurik.monitoring.domain.Vendor;
import com.aurik.monitoring.dto.*;
import com.aurik.monitoring.repository.IngestionRecordRepository;
import com.aurik.monitoring.service.IngestionService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {
    private final IngestionService service;
    private final IngestionRecordRepository repo;

    public IngestionController(IngestionService s, IngestionRecordRepository r) {
        service = s;
        repo = r;
    }

    @PostMapping("/pulseforge")
    public ResponseEntity<IngestionResponse> pulseforge(@RequestBody JsonNode body) {
        return accepted(Vendor.PULSEFORGE, body);
    }

    @PostMapping("/thermexwatch")
    public ResponseEntity<IngestionResponse> thermex(@RequestBody JsonNode body) {
        return accepted(Vendor.THERMEXWATCH, body);
    }

    @PostMapping("/maintaflow")
    public ResponseEntity<IngestionResponse> mainta(@RequestBody JsonNode body) {
        return accepted(Vendor.MAINTAFLOW, body);
    }

    @GetMapping("/{id}")
    public IngestionStatusResponse status(@PathVariable UUID id) {
        var x = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Ingestion not found: " + id));
        return new IngestionStatusResponse(x.getId(), x.getVendor().name(), x.getStatus().name(), x.getReceivedAt(), x.getProcessedAt(), x.getRecordsReceived(), x.getRecordsProcessed(), x.getRecordsDuplicate(), x.getRecordsFailed(), x.getErrorMessage());
    }

    private ResponseEntity<IngestionResponse> accepted(Vendor v, JsonNode b) {
        UUID id = service.accept(v, b);
        return ResponseEntity.accepted().body(new IngestionResponse(id, "ACCEPTED", "Payload accepted for asynchronous processing"));
    }
}
