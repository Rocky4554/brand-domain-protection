package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.ProtectedDomainEntity;
import com.example.BrandProtection.domain.SimilarDomainEntity;
import com.example.BrandProtection.domain.SimilarDomainRepository;
import com.example.BrandProtection.domain.SimilarDomainSource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SimilarDomainService {
    private static final Logger logger = LoggerFactory.getLogger(SimilarDomainService.class);

    private final SimilarDomainRepository similarDomainRepository;

    public SimilarDomainService(SimilarDomainRepository similarDomainRepository) {
        this.similarDomainRepository = similarDomainRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCandidates(
        Set<String> candidates,
        Set<String> domainIqDomains,
        Set<String> generatedDomains,
        ProtectedDomainEntity protectedDomain) {
        logger.info("Persisting {} similar domain candidates for brand {}.",
            candidates.size(), protectedDomain.getBrandDomain());
        similarDomainRepository.deleteByProtectedDomainId(protectedDomain.getId());
        List<SimilarDomainEntity> toSave = new ArrayList<>();
        Instant now = Instant.now();
        for (String domain : candidates) {
            if (domain == null || domain.isBlank()) {
                continue;
            }
            boolean inDomainIq = domainIqDomains.contains(domain);
            boolean inGenerated = generatedDomains.contains(domain);
            SimilarDomainSource source;
            if (inDomainIq && inGenerated) {
                source = SimilarDomainSource.BOTH;
            } else if (inDomainIq) {
                source = SimilarDomainSource.DOMAINIQ;
            } else {
                source = SimilarDomainSource.GENERATED;
            }
            SimilarDomainEntity entity = new SimilarDomainEntity();
            entity.setProtectedDomain(protectedDomain);
            entity.setDomainName(domain);
            entity.setSource(source);
            entity.setCreatedAt(now);
            toSave.add(entity);
        }
        if (toSave.isEmpty()) {
            logger.info("No similar domains to save for brand {}.", protectedDomain.getBrandDomain());
            return;
        }
        similarDomainRepository.saveAll(toSave);
        logger.info("Saved {} similar domains for brand {}.", toSave.size(), protectedDomain.getBrandDomain());
    }
}
