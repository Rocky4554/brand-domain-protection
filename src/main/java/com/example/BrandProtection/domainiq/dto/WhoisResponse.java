package com.example.BrandProtection.domainiq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhoisResponse {
    private String domain;
    private WhoisDetails whois;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public WhoisDetails getWhois() {
        return whois;
    }

    public void setWhois(WhoisDetails whois) {
        this.whois = whois;
    }
}
