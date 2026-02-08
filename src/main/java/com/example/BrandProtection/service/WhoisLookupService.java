package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.WhoisLookupEntity;
import com.example.BrandProtection.domain.WhoisLookupRepository;
import com.example.BrandProtection.domain.WhoisLookupStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WhoisLookupService {
    private static final Logger logger = LoggerFactory.getLogger(WhoisLookupService.class);
    private final WhoisLookupRepository whoisLookupRepository;

    public WhoisLookupService(WhoisLookupRepository whoisLookupRepository) {
        this.whoisLookupRepository = whoisLookupRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedPending(UUID brandId, List<String> domains) {
        whoisLookupRepository.deleteByBrandId(brandId);
        Instant now = Instant.now();
        List<WhoisLookupEntity> batch = new ArrayList<>();
        for (String domain : domains) {
            if (domain == null || domain.isBlank()) {
                continue;
            }
            WhoisLookupEntity entity = new WhoisLookupEntity();
            entity.setBrandId(brandId);
            entity.setDomainName(domain);
            entity.setStatus(WhoisLookupStatus.PENDING);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            batch.add(entity);
        }
        if (!batch.isEmpty()) {
            whoisLookupRepository.saveAll(batch);
        }
        logger.info("WHOIS lookup seeded {} domains for brand {}.", batch.size(), brandId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(UUID brandId, String domain) {
        updateStatus(brandId, domain, WhoisLookupStatus.PROCESSING, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(UUID brandId, String domain, String whoisDetails) {
        updateStatus(brandId, domain, WhoisLookupStatus.COMPLETED, whoisDetails);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID brandId, String domain, String whoisDetails) {
        updateStatus(brandId, domain, WhoisLookupStatus.FAILED, whoisDetails);
    }

    private void updateStatus(UUID brandId, String domain, WhoisLookupStatus status, String whoisDetails) {
        Optional<WhoisLookupEntity> existing =
            whoisLookupRepository.findByBrandIdAndDomainName(brandId, domain);
        WhoisLookupEntity entity = existing.orElseGet(WhoisLookupEntity::new);
        if (entity.getId() == null) {
            entity.setBrandId(brandId);
            entity.setDomainName(domain);
            entity.setCreatedAt(Instant.now());
        }
        entity.setStatus(status);
        entity.setWhoisDetails(whoisDetails);
        entity.setUpdatedAt(Instant.now());
        whoisLookupRepository.save(entity);
    }
}
