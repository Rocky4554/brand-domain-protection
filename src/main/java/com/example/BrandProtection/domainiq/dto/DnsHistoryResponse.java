package com.example.BrandProtection.domainiq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DnsHistoryResponse {
    private String domain;
    private DnsDetails dns;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public DnsDetails getDns() {
        return dns;
    }

    public void setDns(DnsDetails dns) {
        this.dns = dns;
    }
}
