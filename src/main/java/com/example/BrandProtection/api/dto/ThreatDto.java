package com.example.BrandProtection.api.dto;

import com.example.BrandProtection.domain.ThreatSeverity;
import com.example.BrandProtection.domain.ThreatType;
import java.time.Instant;
import java.util.UUID;

public class ThreatDto {
    private UUID id;
    private String domainName;
    private ThreatType type;
    private ThreatSeverity severity;
    private Instant detectedAt;

    public ThreatDto(UUID id, String domainName, ThreatType type, ThreatSeverity severity, Instant detectedAt) {
        this.id = id;
        this.domainName = domainName;
        this.type = type;
        this.severity = severity;
        this.detectedAt = detectedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getDomainName() {
        return domainName;
    }

    public ThreatType getType() {
        return type;
    }

    public ThreatSeverity getSeverity() {
        return severity;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }
}
