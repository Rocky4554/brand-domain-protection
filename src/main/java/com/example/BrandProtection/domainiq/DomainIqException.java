package com.example.BrandProtection.domainiq;

public class DomainIqException extends RuntimeException {
    public DomainIqException(String message) {
        super(message);
    }

    public DomainIqException(String message, Throwable cause) {
        super(message, cause);
    }
}
