package com.example.BrandProtection.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "similar_domains")
public class SimilarDomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private ProtectedDomainEntity protectedDomain;

    @Column(name = "domain_name", nullable = false)
    private String domainName;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private SimilarDomainSource source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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

    public SimilarDomainSource getSource() {
        return source;
    }

    public void setSource(SimilarDomainSource source) {
        this.source = source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
