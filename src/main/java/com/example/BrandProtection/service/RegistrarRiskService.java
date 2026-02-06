package com.example.BrandProtection.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RegistrarRiskService {
    private static final Map<String, RegistrarRiskLevel> REGISTRAR_RISK = Map.ofEntries(
        Map.entry("Alibaba Cloud", RegistrarRiskLevel.HIGH_ABUSE),
        Map.entry("NameCheap", RegistrarRiskLevel.LOW_COST),
        Map.entry("GoDaddy", RegistrarRiskLevel.REPUTABLE)
    );

    public RegistrarRiskLevel assessRisk(String registrar) {
        if (registrar == null || registrar.isBlank()) {
            return RegistrarRiskLevel.LOW_COST;
        }
        return REGISTRAR_RISK.getOrDefault(registrar.trim(), RegistrarRiskLevel.LOW_COST);
    }
}
