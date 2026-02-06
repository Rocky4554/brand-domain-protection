package com.example.BrandProtection.api;

import com.example.BrandProtection.api.dto.RegisterBrandRequest;
import com.example.BrandProtection.api.dto.RegisterBrandResponse;
import com.example.BrandProtection.api.dto.ThreatDto;
import com.example.BrandProtection.api.dto.ThreatSummaryResponse;
import com.example.BrandProtection.service.ProtectedBrandService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/brands")
public class BrandProtectionController {
    private final ProtectedBrandService protectedBrandService;

    public BrandProtectionController(ProtectedBrandService protectedBrandService) {
        this.protectedBrandService = protectedBrandService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterBrandResponse registerBrand(@Valid @RequestBody RegisterBrandRequest request) {
        UUID id = protectedBrandService.registerBrand(request);
        return new RegisterBrandResponse(id);
    }

    @GetMapping("/{id}/threats")
    public ThreatSummaryResponse getThreats(@PathVariable("id") UUID brandId) {
        List<ThreatDto> threats = protectedBrandService.getThreatsForBrand(brandId);
        return new ThreatSummaryResponse(threats.size(), threats);
    }
}
