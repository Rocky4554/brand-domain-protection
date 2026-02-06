package com.example.BrandProtection.service;

public class RiskScoreInput {
    private int similarityScore;
    private int domainAgeDays;
    private int whoisDomainCount;
    private RegistrarRiskLevel registrarRiskLevel;
    private SslRiskFlags sslRiskFlags;
    private ContentRiskFlags contentRiskFlags;
    private boolean mxPresent;
    private boolean suspiciousHostingCountry;
    private boolean registeredDomain;
    private boolean sslPresent;
    private boolean officialSubdomain;
    private boolean approvedRegistrar;
    private boolean approvedEmailProvider;

    public int getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(int similarityScore) {
        this.similarityScore = similarityScore;
    }

    public int getDomainAgeDays() {
        return domainAgeDays;
    }

    public void setDomainAgeDays(int domainAgeDays) {
        this.domainAgeDays = domainAgeDays;
    }

    public int getWhoisDomainCount() {
        return whoisDomainCount;
    }

    public void setWhoisDomainCount(int whoisDomainCount) {
        this.whoisDomainCount = whoisDomainCount;
    }

    public RegistrarRiskLevel getRegistrarRiskLevel() {
        return registrarRiskLevel;
    }

    public void setRegistrarRiskLevel(RegistrarRiskLevel registrarRiskLevel) {
        this.registrarRiskLevel = registrarRiskLevel;
    }

    public SslRiskFlags getSslRiskFlags() {
        return sslRiskFlags;
    }

    public void setSslRiskFlags(SslRiskFlags sslRiskFlags) {
        this.sslRiskFlags = sslRiskFlags;
    }

    public ContentRiskFlags getContentRiskFlags() {
        return contentRiskFlags;
    }

    public void setContentRiskFlags(ContentRiskFlags contentRiskFlags) {
        this.contentRiskFlags = contentRiskFlags;
    }

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

    public boolean isRegisteredDomain() {
        return registeredDomain;
    }

    public void setRegisteredDomain(boolean registeredDomain) {
        this.registeredDomain = registeredDomain;
    }

    public boolean isSslPresent() {
        return sslPresent;
    }

    public void setSslPresent(boolean sslPresent) {
        this.sslPresent = sslPresent;
    }

    public boolean isOfficialSubdomain() {
        return officialSubdomain;
    }

    public void setOfficialSubdomain(boolean officialSubdomain) {
        this.officialSubdomain = officialSubdomain;
    }

    public boolean isApprovedRegistrar() {
        return approvedRegistrar;
    }

    public void setApprovedRegistrar(boolean approvedRegistrar) {
        this.approvedRegistrar = approvedRegistrar;
    }

    public boolean isApprovedEmailProvider() {
        return approvedEmailProvider;
    }

    public void setApprovedEmailProvider(boolean approvedEmailProvider) {
        this.approvedEmailProvider = approvedEmailProvider;
    }
}
