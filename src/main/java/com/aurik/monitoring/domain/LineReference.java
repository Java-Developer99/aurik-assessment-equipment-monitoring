package com.aurik.monitoring.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "line_reference", uniqueConstraints = @UniqueConstraint(name = "uk_plant_line", columnNames = {"plant_id", "line_id"}))
public class LineReference {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "plant_id", nullable = false)
    private String plantId;

    @Column(name = "line_id", nullable = false)
    private String lineId;

    private String lineName, operatingWindow;

    protected LineReference() {
    }

    public LineReference(String p, String l, String n, String w) {
        plantId = p;
        lineId = l;
        lineName = n;
        operatingWindow = w;
    }

    public String getPlantId() {
        return plantId;
    }

    public String getLineId() {
        return lineId;
    }
}
