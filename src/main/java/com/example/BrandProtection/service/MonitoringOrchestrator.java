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

    public void runDiscoveryAndAssessment() {
        List<ProtectedDomainEntity> activeBrands = protectedDomainRepository.findByStatus(BrandStatus.ACTIVE);
        for (ProtectedDomainEntity brand : activeBrands) {
            List<DiscoveredDomainEntity> discoveredDomains = domainDiscoveryService.discover(brand);
            for (DiscoveredDomainEntity discoveredDomain : discoveredDomains) {
                riskAssessmentService.assess(discoveredDomain, brand.getBrandKeyword());
            }
        }
        logger.info("Discovery and assessment completed for {} active brands.", activeBrands.size());
    }

    public void runEnrichmentForPro() {
        List<ProtectedDomainEntity> activeBrands = protectedDomainRepository.findByStatus(BrandStatus.ACTIVE);
        for (ProtectedDomainEntity brand : activeBrands) {
            List<DiscoveredDomainEntity> discoveredDomains = discoveredDomainRepository.findByProtectedDomainId(brand.getId());
            for (DiscoveredDomainEntity discoveredDomain : discoveredDomains) {
                riskAssessmentService.assess(discoveredDomain, brand.getBrandKeyword());
            }
        }
        logger.info("Enrichment completed for {} active brands.", activeBrands.size());
    }

    @Async
    public void runForBrand(ProtectedDomainEntity brand) {
        List<DiscoveredDomainEntity> discoveredDomains = domainDiscoveryService.discover(brand);
        for (DiscoveredDomainEntity discoveredDomain : discoveredDomains) {
            riskAssessmentService.assess(discoveredDomain, brand.getBrandKeyword());
        }
        logger.info("Immediate processing completed for brand {}", brand.getId());
    }
}
