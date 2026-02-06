package com.example.BrandProtection.service;

public class DnsAnalysisResult {
    private boolean mxPresent;
    private boolean suspiciousHostingCountry;
    private java.util.List<String> mxHosts;

    public boolean isMxPresent() {
        return mxPresent;
    }

    public void setMxPresent(boolean mxPresent) {
        this.mxPresent = mxPresent;
    }

    public boolean isSuspiciousHostingCountry() {
        return suspiciousHostingCountry;
    }

    public void setSuspiciousHostingCountry(boolean suspiciousHostingCountry) {
        this.suspiciousHostingCountry = suspiciousHostingCountry;
    }

    public java.util.List<String> getMxHosts() {
        return mxHosts;
    }

    public void setMxHosts(java.util.List<String> mxHosts) {
        this.mxHosts = mxHosts;
    }
}
