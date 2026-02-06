package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.WhoisFingerprintEntity;
import com.example.BrandProtection.domain.WhoisFingerprintRepository;
import com.example.BrandProtection.domainiq.DomainIqClient;
import com.example.BrandProtection.domainiq.dto.WhoisDetails;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WhoisAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(WhoisAnalysisService.class);

    private final DomainIqClient domainIqClient;
    private final WhoisFingerprintRepository whoisFingerprintRepository;

    public WhoisAnalysisService(DomainIqClient domainIqClient, WhoisFingerprintRepository whoisFingerprintRepository) {
        this.domainIqClient = domainIqClient;
        this.whoisFingerprintRepository = whoisFingerprintRepository;
    }

    public WhoisAnalysisResult analyze(String domain) {
        WhoisDetails whois = domainIqClient.getWhoisInfo(domain);
        WhoisAnalysisResult result = new WhoisAnalysisResult();
        result.setWhoisDetails(whois);
        if (whois == null || whois.getRegistrantEmail() == null) {
            result.setAssociatedDomainCount(0);
            return result;
        }

        Optional<WhoisFingerprintEntity> existing = whoisFingerprintRepository.findByRegistrantEmail(whois.getRegistrantEmail());
        WhoisFingerprintEntity entity = existing.orElseGet(WhoisFingerprintEntity::new);
        entity.setRegistrantEmail(whois.getRegistrantEmail());
        entity.setRegistrantOrg(whois.getRegistrantOrg());
        entity.setCountry(whois.getRegistrantCountry());
        Integer count = entity.getAssociatedDomainCount() == null ? 0 : entity.getAssociatedDomainCount();
        entity.setAssociatedDomainCount(count + 1);
        entity.setRiskScore(0);
        whoisFingerprintRepository.save(entity);
        result.setAssociatedDomainCount(entity.getAssociatedDomainCount());

        logger.info("WHOIS correlation for {}: registrant {} has {} domains.", domain, whois.getRegistrantEmail(), entity.getAssociatedDomainCount());
        return result;
    }
}
