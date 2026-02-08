package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.DiscoveredDomainEntity;
import com.example.BrandProtection.domain.ProtectedDomainEntity;
import com.example.BrandProtection.domain.DiscoveredDomainRepository;
import com.example.BrandProtection.domainiq.DomainIqClient;
import com.example.BrandProtection.domainiq.dto.DnsDetails;
import com.example.BrandProtection.domainiq.dto.DomainSearchResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
    private final DomainDiscoveryProperties discoveryProperties;
    private final SimilarDomainService similarDomainService;
    private final GeneratedDomainService generatedDomainService;
    private final ObjectMapper objectMapper;
 

    public DomainDiscoveryService(
        DomainIqClient domainIqClient,
        SimilarityService similarityService,
        DiscoveredDomainRepository discoveredDomainRepository,
        DomainVariationGenerator variationGenerator,
        DomainDiscoveryProperties discoveryProperties,
        SimilarDomainService similarDomainService,
        GeneratedDomainService generatedDomainService,
        ObjectMapper objectMapper) {
        this.domainIqClient = domainIqClient;
        this.similarityService = similarityService;
        this.discoveredDomainRepository = discoveredDomainRepository;
        this.variationGenerator = variationGenerator;
        this.discoveryProperties = discoveryProperties;
        this.similarDomainService = similarDomainService;
        this.generatedDomainService = generatedDomainService;
        this.objectMapper = objectMapper;
    }

    public List<DiscoveredDomainEntity> discover(BrandSnapshot snapshot, ProtectedDomainEntity protectedDomain) {
        logger.info("Discovery started for brand {}", snapshot.getBrandDomain());
        Set<String> candidateDomains = new LinkedHashSet<>();
        String keyword = snapshot.getBrandKeyword() + "*";
        List<DomainSearchResult> results = domainIqClient.searchDomainsByKeyword(keyword);
        int maxDomainIq = discoveryProperties.getMaxDomainIqResults();
        if (maxDomainIq > 0 && results.size() > maxDomainIq) {
            logger.info("Limiting DomainIQ results from {} to {}.", results.size(), maxDomainIq);
            results = results.subList(0, maxDomainIq);
        }
        Set<String> domainIqDomains = results.stream()
            .map(DomainSearchResult::getDomain)
            .filter(domain -> domain != null && !domain.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        candidateDomains.addAll(domainIqDomains);

        Set<String> generated = variationGenerator.generate(
            snapshot.getBrandDomain(),
            snapshot.getKeywords());
        int maxGenerated = discoveryProperties.getMaxGenerated();
        if (maxGenerated > 0 && generated.size() > maxGenerated) {
            logger.info("Limiting generated candidates from {} to {}.", generated.size(), maxGenerated);
            generated = generated.stream()
                .limit(maxGenerated)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        candidateDomains.addAll(generated);
        int maxCandidates = discoveryProperties.getMaxCandidates();
        if (maxCandidates > 0 && candidateDomains.size() > maxCandidates) {
            logger.info("Limiting total candidates from {} to {}.", candidateDomains.size(), maxCandidates);
            candidateDomains = candidateDomains.stream()
                .limit(maxCandidates)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        logger.info("Discovery candidates: domainIq={}, generated={}, total={}",
            domainIqDomains.size(), generated.size(), candidateDomains.size());

        similarDomainService.saveCandidates(candidateDomains, domainIqDomains, generated, protectedDomain);
        logger.info("Similar domain saving finished for brand {}.", snapshot.getBrandDomain());
        generatedDomainService.seedGeneratedDomains(generated, protectedDomain);

        List<DiscoveredDomainEntity> discovered = new ArrayList<>();

        for (String domain : candidateDomains) {
            boolean fromDomainIq = domainIqDomains.contains(domain);
            boolean fromGenerated = generated.contains(domain);
            if (fromGenerated && fromDomainIq) {
                generatedDomainService.markFulfilled(
                    protectedDomain,
                    domain,
                    "{\"skipped\":true,\"reason\":\"present_in_domainiq\"}");
                logger.info("DNS fetch skipped for {} (present in DomainIQ).", domain);
            }
            if (!fromDomainIq && !isRegisteredDomain(domain, fromGenerated, protectedDomain)) {
                logger.info("DNS fetch skipped for {}.", domain);
                continue;
            }
            int similarity = similarityService.similarityPercent(domain, snapshot.getBrandKeyword());
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

        logger.info("DNS fetching completed for brand {}.", snapshot.getBrandDomain());
        logger.info("Discovered {} suspicious domains for brand {}", discovered.size(), snapshot.getBrandDomain());
        return discoveredDomainRepository.saveAll(discovered);
    }

    private Instant parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        String trimmed = date.trim();
        if ("0000-00-00".equals(trimmed)) {
            return null;
        }
        try {
            LocalDate parsed = LocalDate.parse(trimmed, DATE_FORMATTER);
            return parsed.atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (RuntimeException ex) {
            logger.warn("Skipping invalid date from DomainIQ: {}", trimmed);
            return null;
        }
    }

    private boolean isRegisteredDomain(String domain, boolean isGenerated, ProtectedDomainEntity brand) {
        if (!isGenerated) {
            return false;
        }
        logger.info("DNS fetching started for {}.", domain);
        generatedDomainService.markProcessing(brand, domain);
        try {
            DnsDetails dnsDetails = domainIqClient.getDnsHistory(domain);
            if (dnsDetails == null) {
                generatedDomainService.markFailed(brand, domain, "No DNS details returned");
                logger.info("DNS fetching completed for {} (no DNS details).", domain);
                return false;
            }
            boolean hasA = dnsDetails.getaRecords() != null && !dnsDetails.getaRecords().isEmpty();
            boolean hasMx = dnsDetails.getMxRecords() != null && !dnsDetails.getMxRecords().isEmpty();
            boolean hasNs = dnsDetails.getNsRecords() != null && !dnsDetails.getNsRecords().isEmpty();
            String dnsJson = toJson(dnsDetails);
            if (hasA || hasMx || hasNs) {
                generatedDomainService.markFulfilled(brand, domain, dnsJson);
                logger.info("DNS fetching completed for {} (records found).", domain);
                return true;
            }
            generatedDomainService.markFailed(brand, domain, dnsJson);
            logger.info("DNS fetching completed for {} (no records).", domain);
            return false;
        } catch (RuntimeException ex) {
            generatedDomainService.markFailed(brand, domain, "DomainIQ error: " + ex.getMessage());
            logger.warn("DNS check skipped for {} due to DomainIQ error: {}", domain, ex.getMessage());
            logger.info("DNS fetching completed for {} (error).", domain);
            return false;
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"Failed to serialize DNS result\"}";
        }
    }
}
