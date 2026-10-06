package com.aurik.monitoring.config;

import com.aurik.monitoring.domain.*;
import com.aurik.monitoring.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class ReferenceDataLoader implements CommandLineRunner {
    private final MachineReferenceRepository machines;
    private final LineReferenceRepository lines;

    public ReferenceDataLoader(MachineReferenceRepository machines, LineReferenceRepository lines) {
        this.machines = machines;
        this.lines = lines;
    }

    public void run(String... args) throws Exception {
        if (machines.count() == 0) loadMachines();
        if (lines.count() == 0) loadLines();
    }

    private void loadMachines() throws Exception {
        try (BufferedReader r = reader("reference/asset_reference.csv")) {
            r.readLine();
            String s;
            while ((s = r.readLine()) != null) {
                String[] c = s.split(",", -1);
                machines.save(new MachineReference(c[0], c[1], c[2], c[3], c[4], Double.valueOf(c[6]), Double.valueOf(c[7]), Double.valueOf(c[8]), c[9]));
            }
        }
    }

    private void loadLines() throws Exception {
        try (BufferedReader r = reader("reference/line_reference.csv")) {
            r.readLine();
            String s;
            while ((s = r.readLine()) != null) {
                String[] c = s.split(",", -1);
                lines.save(new LineReference(c[0], c[1], c[2], c[3]));
            }
        }
    }

    private BufferedReader reader(String p) throws IOException {
        return new BufferedReader(new InputStreamReader(new ClassPathResource(p).getInputStream(), StandardCharsets.UTF_8));
    }
}
