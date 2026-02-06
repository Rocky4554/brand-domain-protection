package com.example.BrandProtection.service;

import com.example.BrandProtection.api.dto.RegisterBrandRequest;
import com.example.BrandProtection.api.dto.ThreatDto;
import com.example.BrandProtection.domain.BrandStatus;
import com.example.BrandProtection.domain.DiscoveredDomainEntity;
import com.example.BrandProtection.domain.DiscoveredDomainRepository;
import com.example.BrandProtection.domain.ProtectedDomainEntity;
import com.example.BrandProtection.domain.ProtectedDomainRepository;
import com.example.BrandProtection.domain.ThreatEntity;
import com.example.BrandProtection.domain.ThreatRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProtectedBrandService {
    private final ProtectedDomainRepository protectedDomainRepository;
    private final DiscoveredDomainRepository discoveredDomainRepository;
    private final ThreatRepository threatRepository;
    private final MonitoringOrchestrator monitoringOrchestrator;

    public ProtectedBrandService(
        ProtectedDomainRepository protectedDomainRepository,
        DiscoveredDomainRepository discoveredDomainRepository,
        ThreatRepository threatRepository,
        MonitoringOrchestrator monitoringOrchestrator) {
        this.protectedDomainRepository = protectedDomainRepository;
        this.discoveredDomainRepository = discoveredDomainRepository;
        this.threatRepository = threatRepository;
        this.monitoringOrchestrator = monitoringOrchestrator;
    }

    public UUID registerBrand(RegisterBrandRequest request) {
        ProtectedDomainEntity entity = new ProtectedDomainEntity();
        entity.setUserId(request.getUserId());
        entity.setBrandDomain(request.getBrandDomain());
        entity.setBrandKeyword(request.getBrandKeyword());
        entity.setBrandName(request.getBrandName());
        entity.setKeywords(request.getKeywords());
        entity.setOfficialSubdomains(request.getOfficialSubdomains());
        entity.setApprovedRegistrars(request.getApprovedRegistrars());
        entity.setApprovedEmailProviders(request.getApprovedEmailProviders());
        entity.setStatus(BrandStatus.ACTIVE);
        entity.setCreatedAt(Instant.now());
        ProtectedDomainEntity saved = protectedDomainRepository.save(entity);
        org.slf4j.LoggerFactory.getLogger(ProtectedBrandService.class)
            .info("Brand registered: id={}, domain={}, keywords={}",
                saved.getId(), saved.getBrandDomain(), saved.getKeywords());
        monitoringOrchestrator.runForBrand(saved);
        return saved.getId();
    }

    public List<ThreatDto> getThreatsForBrand(UUID brandId) {
        List<DiscoveredDomainEntity> discoveredDomains = discoveredDomainRepository.findByProtectedDomainId(brandId);
        List<ThreatDto> threats = new ArrayList<>();
        for (DiscoveredDomainEntity discoveredDomain : discoveredDomains) {
            List<ThreatEntity> domainThreats = threatRepository.findByDomainName(discoveredDomain.getDomainName());
            for (ThreatEntity threat : domainThreats) {
                threats.add(new ThreatDto(
                    threat.getId(),
                    threat.getDomainName(),
                    threat.getType(),
                    threat.getSeverity(),
                    threat.getDetectedAt()));
            }
        }
        return threats;
    }
}
