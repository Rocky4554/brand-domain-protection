package com.example.BrandProtection.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class RegisterBrandRequest {
    @NotNull
    private UUID userId;

    @NotBlank
    private String brandDomain;

    @NotBlank
    private String brandKeyword;

    @NotBlank
    private String brandName;

    @NotNull
    private List<String> keywords;

    @NotNull
    private List<String> officialSubdomains;

    @NotNull
    private List<String> approvedRegistrars;

    @NotNull
    private List<String> approvedEmailProviders;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
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
