package com.example.BrandProtection.api.dto;

import java.util.List;

public class ThreatSummaryResponse {
    private int totalThreats;
    private List<ThreatDto> threats;

    public ThreatSummaryResponse(int totalThreats, List<ThreatDto> threats) {
        this.totalThreats = totalThreats;
        this.threats = threats;
    }

    public int getTotalThreats() {
        return totalThreats;
    }

    public List<ThreatDto> getThreats() {
        return threats;
    }
}
