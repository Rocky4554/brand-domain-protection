package com.example.BrandProtection.service;

public class SslRiskFlags {
    private boolean issuedRecently;
    private boolean shortValidity;
    private boolean unknownIssuer;

    public boolean isIssuedRecently() {
        return issuedRecently;
    }

    public void setIssuedRecently(boolean issuedRecently) {
        this.issuedRecently = issuedRecently;
    }

    public boolean isShortValidity() {
        return shortValidity;
    }

    public void setShortValidity(boolean shortValidity) {
        this.shortValidity = shortValidity;
    }

    public boolean isUnknownIssuer() {
        return unknownIssuer;
    }

    public void setUnknownIssuer(boolean unknownIssuer) {
        this.unknownIssuer = unknownIssuer;
    }
}
