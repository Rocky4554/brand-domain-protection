package com.example.BrandProtection.service;

public class ContentRiskFlags {
    private boolean loginFormDetected;
    private boolean brandKeywordsPresent;
    private boolean redirectChain;

    public boolean isLoginFormDetected() {
        return loginFormDetected;
    }

    public void setLoginFormDetected(boolean loginFormDetected) {
        this.loginFormDetected = loginFormDetected;
    }

    public boolean isBrandKeywordsPresent() {
        return brandKeywordsPresent;
    }

    public void setBrandKeywordsPresent(boolean brandKeywordsPresent) {
        this.brandKeywordsPresent = brandKeywordsPresent;
    }

    public boolean isRedirectChain() {
        return redirectChain;
    }

    public void setRedirectChain(boolean redirectChain) {
        this.redirectChain = redirectChain;
    }
}
