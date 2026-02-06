package com.example.BrandProtection.api.dto;

import java.util.UUID;

public class RegisterBrandResponse {
    private UUID id;

    public RegisterBrandResponse(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
