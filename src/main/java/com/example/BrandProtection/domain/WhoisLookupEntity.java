package com.example.BrandProtection.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "whois_lookup")
public class WhoisLookupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "brand_id", nullable = false)
    private UUID brandId;

    @Column(name = "domain_name", nullable = false)
    private String domainName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WhoisLookupStatus status;

    @Lob
    @Column(name = "whois_details")
    private String whoisDetails;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBrandId() {
        return brandId;
    }

    public void setBrandId(UUID brandId) {
        this.brandId = brandId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public WhoisLookupStatus getStatus() {
        return status;
    }

    public void setStatus(WhoisLookupStatus status) {
        this.status = status;
    }

    public String getWhoisDetails() {
        return whoisDetails;
    }

    public void setWhoisDetails(String whoisDetails) {
        this.whoisDetails = whoisDetails;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
