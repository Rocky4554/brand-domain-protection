package com.example.BrandProtection.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "whois_fingerprints")
public class WhoisFingerprintEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "registrant_email")
    private String registrantEmail;

    @Column(name = "registrant_org")
    private String registrantOrg;

    private String country;

    @Column(name = "associated_domain_count")
    private Integer associatedDomainCount;

    @Column(name = "risk_score")
    private Integer riskScore;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRegistrantEmail() {
        return registrantEmail;
    }

    public void setRegistrantEmail(String registrantEmail) {
        this.registrantEmail = registrantEmail;
    }

    public String getRegistrantOrg() {
        return registrantOrg;
    }

    public void setRegistrantOrg(String registrantOrg) {
        this.registrantOrg = registrantOrg;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getAssociatedDomainCount() {
        return associatedDomainCount;
    }

    public void setAssociatedDomainCount(Integer associatedDomainCount) {
        this.associatedDomainCount = associatedDomainCount;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }
}
