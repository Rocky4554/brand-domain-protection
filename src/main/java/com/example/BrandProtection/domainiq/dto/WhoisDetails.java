package com.example.BrandProtection.domainiq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhoisDetails {
    private String registrar;
    @JsonProperty("creation_date")
    private String creationDate;
    @JsonProperty("expiration_date")
    private String expirationDate;
    @JsonProperty("updated_date")
    private String updatedDate;
    @JsonProperty("registrant_country")
    private String registrantCountry;
    @JsonProperty("privacy_protected")
    private Boolean privacyProtected;
    @JsonProperty("registrant_name")
    private String registrantName;
    @JsonProperty("registrant_email")
    private String registrantEmail;
    @JsonProperty("registrant_org")
    private String registrantOrg;

    public String getRegistrar() {
        return registrar;
    }

    public void setRegistrar(String registrar) {
        this.registrar = registrar;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getRegistrantCountry() {
        return registrantCountry;
    }

    public void setRegistrantCountry(String registrantCountry) {
        this.registrantCountry = registrantCountry;
    }

    public Boolean getPrivacyProtected() {
        return privacyProtected;
    }

    public void setPrivacyProtected(Boolean privacyProtected) {
        this.privacyProtected = privacyProtected;
    }

    public String getRegistrantName() {
        return registrantName;
    }

    public void setRegistrantName(String registrantName) {
        this.registrantName = registrantName;
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
}
