package com.example.BrandProtection.domainiq.dto;

public class DomainSearchResult {
    private String domain;
    private String registrationDate;
    private String registrar;
    private String country;

    public DomainSearchResult() {
    }

    public DomainSearchResult(String domain, String registrationDate, String registrar, String country) {
        this.domain = domain;
        this.registrationDate = registrationDate;
        this.registrar = registrar;
        this.country = country;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getRegistrar() {
        return registrar;
    }

    public void setRegistrar(String registrar) {
        this.registrar = registrar;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
