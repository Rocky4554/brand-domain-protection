package com.example.BrandProtection.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "generated_domains")
public class GeneratedDomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private ProtectedDomainEntity protectedDomain;

    @Column(name = "domain_name", nullable = false)
    private String domainName;

    @Enumerated(EnumType.STRING)
    @Column(name = "dns_status", nullable = false)
    private GeneratedDomainStatus dnsStatus;

    @Lob
    @Column(name = "dns_result")
    private String dnsResult;

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

    public ProtectedDomainEntity getProtectedDomain() {
        return protectedDomain;
    }

    public void setProtectedDomain(ProtectedDomainEntity protectedDomain) {
        this.protectedDomain = protectedDomain;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public GeneratedDomainStatus getDnsStatus() {
        return dnsStatus;
    }

    public void setDnsStatus(GeneratedDomainStatus dnsStatus) {
        this.dnsStatus = dnsStatus;
    }

    public String getDnsResult() {
        return dnsResult;
    }

    public void setDnsResult(String dnsResult) {
        this.dnsResult = dnsResult;
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
