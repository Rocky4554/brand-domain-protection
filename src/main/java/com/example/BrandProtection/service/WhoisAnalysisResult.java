package com.example.BrandProtection.service;

import com.example.BrandProtection.domainiq.dto.WhoisDetails;

public class WhoisAnalysisResult {
    private WhoisDetails whoisDetails;
    private int associatedDomainCount;

    public WhoisDetails getWhoisDetails() {
        return whoisDetails;
    }

    public void setWhoisDetails(WhoisDetails whoisDetails) {
        this.whoisDetails = whoisDetails;
    }

    public int getAssociatedDomainCount() {
        return associatedDomainCount;
    }

    public void setAssociatedDomainCount(int associatedDomainCount) {
        this.associatedDomainCount = associatedDomainCount;
    }
}
