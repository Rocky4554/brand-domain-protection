package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.GeneratedDomainEntity;
import com.example.BrandProtection.domain.GeneratedDomainRepository;
import com.example.BrandProtection.domain.GeneratedDomainStatus;
import com.example.BrandProtection.domain.ProtectedDomainEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeneratedDomainService {
    private final GeneratedDomainRepository generatedDomainRepository;

    public GeneratedDomainService(GeneratedDomainRepository generatedDomainRepository) {
        this.generatedDomainRepository = generatedDomainRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedGeneratedDomains(Set<String> generatedDomains, ProtectedDomainEntity brand) {
        generatedDomainRepository.deleteByProtectedDomainId(brand.getId());
        Instant now = Instant.now();
        for (String domain : generatedDomains) {
            if (domain == null || domain.isBlank()) {
                continue;
            }
            GeneratedDomainEntity entity = new GeneratedDomainEntity();
            entity.setProtectedDomain(brand);
            entity.setDomainName(domain);
            entity.setDnsStatus(GeneratedDomainStatus.PENDING);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            generatedDomainRepository.save(entity);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(ProtectedDomainEntity brand, String domain) {
        updateStatus(brand, domain, GeneratedDomainStatus.PROCESSING, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFulfilled(ProtectedDomainEntity brand, String domain, String dnsResult) {
        updateStatus(brand, domain, GeneratedDomainStatus.FULFILLED, dnsResult);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(ProtectedDomainEntity brand, String domain, String dnsResult) {
        updateStatus(brand, domain, GeneratedDomainStatus.FAILED, dnsResult);
    }

    private void updateStatus(ProtectedDomainEntity brand, String domain, GeneratedDomainStatus status, String dnsResult) {
        Optional<GeneratedDomainEntity> existing =
            generatedDomainRepository.findByProtectedDomainIdAndDomainName(brand.getId(), domain);
        GeneratedDomainEntity entity = existing.orElseGet(GeneratedDomainEntity::new);
        if (entity.getId() == null) {
            entity.setProtectedDomain(brand);
            entity.setDomainName(domain);
            entity.setCreatedAt(Instant.now());
        }
        entity.setDnsStatus(status);
        entity.setDnsResult(dnsResult);
        entity.setUpdatedAt(Instant.now());
        generatedDomainRepository.save(entity);
    }
}
