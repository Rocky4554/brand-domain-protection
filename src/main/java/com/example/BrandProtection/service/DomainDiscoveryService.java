package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.DiscoveredDomainEntity;
import com.example.BrandProtection.domain.DiscoveredDomainRepository;
import com.example.BrandProtection.domain.ProtectedDomainEntity;
import com.example.BrandProtection.domainiq.DomainIqClient;
import com.example.BrandProtection.domainiq.dto.DnsDetails;
import com.example.BrandProtection.domainiq.dto.DomainSearchResult;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DomainDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(DomainDiscoveryService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final int SIMILARITY_THRESHOLD = 70;

    private final DomainIqClient domainIqClient;
    private final SimilarityService similarityService;
    private final DiscoveredDomainRepository discoveredDomainRepository;
    private final DomainVariationGenerator variationGenerator;

    public DomainDiscoveryService(
        DomainIqClient domainIqClient,
        SimilarityService similarityService,
        DiscoveredDomainRepository discoveredDomainRepository,
        DomainVariationGenerator variationGenerator) {
        this.domainIqClient = domainIqClient;
        this.similarityService = similarityService;
        this.discoveredDomainRepository = discoveredDomainRepository;
        this.variationGenerator = variationGenerator;
    }

    public List<DiscoveredDomainEntity> discover(ProtectedDomainEntity protectedDomain) {
        Set<String> candidateDomains = new HashSet<>();
        String keyword = protectedDomain.getBrandKeyword() + "*";
        List<DomainSearchResult> results = domainIqClient.searchDomainsByKeyword(keyword);
        Set<String> domainIqDomains = results.stream()
            .map(DomainSearchResult::getDomain)
            .filter(domain -> domain != null && !domain.isBlank())
            .collect(Collectors.toSet());
        candidateDomains.addAll(domainIqDomains);

        Set<String> generated = variationGenerator.generate(
            protectedDomain.getBrandDomain(),
            protectedDomain.getKeywords());
        candidateDomains.addAll(generated);

        List<DiscoveredDomainEntity> discovered = new ArrayList<>();

        for (String domain : candidateDomains) {
            if (!domainIqDomains.contains(domain) && !isRegisteredDomain(domain)) {
                continue;
            }
            int similarity = similarityService.similarityPercent(domain, protectedDomain.getBrandKeyword());
            if (similarity < SIMILARITY_THRESHOLD) {
                continue;
            }
            DiscoveredDomainEntity entity = new DiscoveredDomainEntity();
            entity.setProtectedDomain(protectedDomain);
            entity.setDomainName(domain);
            entity.setSimilarityScore((double) similarity);
            DomainSearchResult match = results.stream()
                .filter(result -> domain.equalsIgnoreCase(result.getDomain()))
                .findFirst()
                .orElse(null);
            if (match != null) {
                entity.setRegistrar(match.getRegistrar());
                entity.setCountry(match.getCountry());
                entity.setFirstSeen(parseDate(match.getRegistrationDate()));
            }
            entity.setLastChecked(Instant.now());
            discovered.add(entity);
        }

        logger.info("Discovered {} suspicious domains for brand {}", discovered.size(), protectedDomain.getBrandDomain());
        return discoveredDomainRepository.saveAll(discovered);
    }

    private Instant parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        LocalDate parsed = LocalDate.parse(date, DATE_FORMATTER);
        return parsed.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private boolean isRegisteredDomain(String domain) {
        DnsDetails dnsDetails = domainIqClient.getDnsHistory(domain);
        if (dnsDetails == null) {
            return false;
        }
        boolean hasA = dnsDetails.getaRecords() != null && !dnsDetails.getaRecords().isEmpty();
        boolean hasMx = dnsDetails.getMxRecords() != null && !dnsDetails.getMxRecords().isEmpty();
        boolean hasNs = dnsDetails.getNsRecords() != null && !dnsDetails.getNsRecords().isEmpty();
        return hasA || hasMx || hasNs;
    }
}
