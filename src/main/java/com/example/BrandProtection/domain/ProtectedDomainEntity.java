package com.example.BrandProtection.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "registered_domains")
public class ProtectedDomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "brand_domain", nullable = false)
    private String brandDomain;

    @Column(name = "brand_keyword", nullable = false)
    private String brandKeyword;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @ElementCollection
    @CollectionTable(name = "brand_keywords", joinColumns = @JoinColumn(name = "brand_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "brand_official_subdomains", joinColumns = @JoinColumn(name = "brand_id"))
    @Column(name = "subdomain")
    private List<String> officialSubdomains = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "brand_approved_registrars", joinColumns = @JoinColumn(name = "brand_id"))
    @Column(name = "registrar")
    private List<String> approvedRegistrars = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "brand_approved_email_providers", joinColumns = @JoinColumn(name = "brand_id"))
    @Column(name = "email_provider")
    private List<String> approvedEmailProviders = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrandStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public BrandStatus getStatus() {
        return status;
    }

    public void setStatus(BrandStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
