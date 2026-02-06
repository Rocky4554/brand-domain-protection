package com.example.BrandProtection.domainiq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DnsDetails {
    @JsonProperty("a_records")
    private List<DnsRecord> aRecords;
    @JsonProperty("ns_records")
    private List<String> nsRecords;
    @JsonProperty("mx_records")
    private List<MxRecord> mxRecords;

    public List<DnsRecord> getaRecords() {
        return aRecords;
    }

    public void setaRecords(List<DnsRecord> aRecords) {
        this.aRecords = aRecords;
    }

    public List<String> getNsRecords() {
        return nsRecords;
    }

    public void setNsRecords(List<String> nsRecords) {
        this.nsRecords = nsRecords;
    }

    public List<MxRecord> getMxRecords() {
        return mxRecords;
    }

    public void setMxRecords(List<MxRecord> mxRecords) {
        this.mxRecords = mxRecords;
    }
}
