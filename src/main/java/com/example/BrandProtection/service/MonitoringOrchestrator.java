package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.BrandStatus;
import com.example.BrandProtection.domain.DiscoveredDomainEntity;
import com.example.BrandProtection.domain.DiscoveredDomainRepository;
import com.example.BrandProtection.domain.ProtectedDomainEntity;
import com.example.BrandProtection.domain.ProtectedDomainRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitoringOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringOrchestrator.class);

    private final ProtectedDomainRepository protectedDomainRepository;
    private final DiscoveredDomainRepository discoveredDomainRepository;
    private final DomainDiscoveryService domainDiscoveryService;
    private final DomainRiskAssessmentService riskAssessmentService;

    public MonitoringOrchestrator(
        ProtectedDomainRepository protectedDomainRepository,
        DiscoveredDomainRepository discoveredDomainRepository,
        DomainDiscoveryService domainDiscoveryService,
        DomainRiskAssessmentService riskAssessmentService) {
        this.protectedDomainRepository = protectedDomainRepository;
        this.discoveredDomainRepository = discoveredDomainRepository;
        this.domainDiscoveryService = domainDiscoveryService;
        this.riskAssessmentService = riskAssessmentService;
    }

    @Transactional
    public void runDiscoveryAndAssessment() {
        List<ProtectedDomainEntity> activeBrands = protectedDomainRepository.findByStatus(BrandStatus.ACTIVE);
        for (ProtectedDomainEntity brand : activeBrands) {
            BrandSnapshot snapshot = toSnapshot(brand);
            List<DiscoveredDomainEntity> discoveredDomains = domainDiscoveryService.discover(snapshot, brand);
            for (DiscoveredDomainEntity discoveredDomain : discoveredDomains) {
                riskAssessmentService.assess(discoveredDomain, snapshot);
            }
        }
        logger.info("Discovery and assessment completed for {} active brands.", activeBrands.size());
    }

    @Transactional
    public void runEnrichmentForPro() {
        List<ProtectedDomainEntity> activeBrands = protectedDomainRepository.findByStatus(BrandStatus.ACTIVE);
        for (ProtectedDomainEntity brand : activeBrands) {
            BrandSnapshot snapshot = toSnapshot(brand);
            List<DiscoveredDomainEntity> discoveredDomains = discoveredDomainRepository.findByProtectedDomainId(brand.getId());
            for (DiscoveredDomainEntity discoveredDomain : discoveredDomains) {
                riskAssessmentService.assess(discoveredDomain, snapshot);
            }
        }
        logger.info("Enrichment completed for {} active brands.", activeBrands.size());
    }

    @Transactional
    public void runForBrand(ProtectedDomainEntity brand) {
        BrandSnapshot snapshot = toSnapshot(brand);
        runForBrandAsync(snapshot, brand);
    }

    @Async
    @Transactional
    public void runForBrandId(java.util.UUID brandId) {
        ProtectedDomainEntity brand = protectedDomainRepository.findById(brandId).orElse(null);
        if (brand == null) {
            logger.warn("Discovery skipped. Brand not found: {}", brandId);
            return;
        }
        BrandSnapshot snapshot = toSnapshot(brand);
        runForBrandAsync(snapshot, brand);
    }

    @Async
    public void runForBrandAsync(BrandSnapshot snapshot, ProtectedDomainEntity brand) {
        logger.info("Running discovery for brand {}", snapshot.getBrandDomain());
        List<DiscoveredDomainEntity> discoveredDomains = domainDiscoveryService.discover(snapshot, brand);
        for (DiscoveredDomainEntity discoveredDomain : discoveredDomains) {
            riskAssessmentService.assess(discoveredDomain, snapshot);
        }
        logger.info("Immediate processing completed for brand {}", snapshot.getId());
    }

    private BrandSnapshot toSnapshot(ProtectedDomainEntity brand) {
        BrandSnapshot snapshot = new BrandSnapshot();
        snapshot.setId(brand.getId());
        snapshot.setBrandDomain(brand.getBrandDomain());
        snapshot.setBrandKeyword(brand.getBrandKeyword());
        snapshot.setKeywords(java.util.List.copyOf(brand.getKeywords()));
        snapshot.setOfficialSubdomains(java.util.List.copyOf(brand.getOfficialSubdomains()));
        snapshot.setApprovedRegistrars(java.util.List.copyOf(brand.getApprovedRegistrars()));
        snapshot.setApprovedEmailProviders(java.util.List.copyOf(brand.getApprovedEmailProviders()));
        return snapshot;
    }
}
