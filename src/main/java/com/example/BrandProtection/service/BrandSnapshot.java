package com.example.BrandProtection.service;

import java.util.List;
import java.util.UUID;

public class BrandSnapshot {
    private UUID id;
    private String brandDomain;
    private String brandKeyword;
    private List<String> keywords;
    private List<String> officialSubdomains;
    private List<String> approvedRegistrars;
    private List<String> approvedEmailProviders;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBrandDomain() {
        return brandDomain;
    }

    public void setBrandDomain(String brandDomain) {
        this.brandDomain = brandDomain;
    }

    public String getBrandKeyword() {
        return brandKeyword;
    }

    public void setBrandKeyword(String brandKeyword) {
        this.brandKeyword = brandKeyword;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getOfficialSubdomains() {
        return officialSubdomains;
    }

    public void setOfficialSubdomains(List<String> officialSubdomains) {
        this.officialSubdomains = officialSubdomains;
    }

    public List<String> getApprovedRegistrars() {
        return approvedRegistrars;
    }

    public void setApprovedRegistrars(List<String> approvedRegistrars) {
        this.approvedRegistrars = approvedRegistrars;
    }

    public List<String> getApprovedEmailProviders() {
        return approvedEmailProviders;
    }

    public void setApprovedEmailProviders(List<String> approvedEmailProviders) {
        this.approvedEmailProviders = approvedEmailProviders;
    }
}
