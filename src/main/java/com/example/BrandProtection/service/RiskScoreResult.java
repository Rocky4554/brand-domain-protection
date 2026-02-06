package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.RiskLevel;
import java.util.Map;

public class RiskScoreResult {
    private int finalScore;
    private RiskLevel riskLevel;
    private Map<String, Integer> breakdown;

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Map<String, Integer> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(Map<String, Integer> breakdown) {
        this.breakdown = breakdown;
    }
}
