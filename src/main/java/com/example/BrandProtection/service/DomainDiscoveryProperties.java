package com.example.BrandProtection.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discovery")
public class DomainDiscoveryProperties {
    @NotNull
    @Min(1L)
    private Integer maxDomainIqResults;

    @NotNull
    @Min(1L)
    private Integer maxGenerated;

    @NotNull
    @Min(1L)
    private Integer maxCandidates;

    public Integer getMaxDomainIqResults() {
        return maxDomainIqResults;
    }

    public void setMaxDomainIqResults(Integer maxDomainIqResults) {
        this.maxDomainIqResults = maxDomainIqResults;
    }

    public Integer getMaxGenerated() {
        return maxGenerated;
    }

    public void setMaxGenerated(Integer maxGenerated) {
        this.maxGenerated = maxGenerated;
    }

    public Integer getMaxCandidates() {
        return maxCandidates;
    }

    public void setMaxCandidates(Integer maxCandidates) {
        this.maxCandidates = maxCandidates;
    }
}
