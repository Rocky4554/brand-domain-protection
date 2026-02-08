package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.DiscoveredDomainEntity;
import com.example.BrandProtection.domain.DiscoveredDomainRepository;
import com.example.BrandProtection.domain.RiskLevel;
import com.example.BrandProtection.domainiq.dto.WhoisDetails;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DomainRiskAssessmentService {
    private static final Logger logger = LoggerFactory.getLogger(DomainRiskAssessmentService.class);

    private final DomainAgeCalculator domainAgeCalculator;
    private final WhoisAnalysisService whoisAnalysisService;
    private final RegistrarRiskService registrarRiskService;
    private final SslAnalysisService sslAnalysisService;
    private final ContentAnalysisService contentAnalysisService;
    private final PhishingRiskScoringService scoringService;
    private final ThreatService threatService;
    private final DiscoveredDomainRepository discoveredDomainRepository;
    private final DnsAnalysisService dnsAnalysisService;

    public DomainRiskAssessmentService(
        DomainAgeCalculator domainAgeCalculator,
        WhoisAnalysisService whoisAnalysisService,
        RegistrarRiskService registrarRiskService,
        SslAnalysisService sslAnalysisService,
        ContentAnalysisService contentAnalysisService,
        PhishingRiskScoringService scoringService,
        ThreatService threatService,
        DiscoveredDomainRepository discoveredDomainRepository,
        DnsAnalysisService dnsAnalysisService) {
        this.domainAgeCalculator = domainAgeCalculator;
        this.whoisAnalysisService = whoisAnalysisService;
        this.registrarRiskService = registrarRiskService;
        this.sslAnalysisService = sslAnalysisService;
        this.contentAnalysisService = contentAnalysisService;
        this.scoringService = scoringService;
        this.threatService = threatService;
        this.discoveredDomainRepository = discoveredDomainRepository;
        this.dnsAnalysisService = dnsAnalysisService;
    }

    public RiskScoreResult assess(DiscoveredDomainEntity discoveredDomain, BrandSnapshot snapshot) {
        String domain = discoveredDomain.getDomainName();
        logger.info("Risk assessment started for domain {}", domain);
        WhoisAnalysisResult whoisResult = null;
        WhoisDetails whoisDetails = null;
        int domainAgeDays = -1;
        RegistrarRiskLevel registrarRisk = registrarRiskService.assessRisk(discoveredDomain.getRegistrar());
        SslInspectionResult sslResult = null;
        SslRiskFlags sslRiskFlags = new SslRiskFlags();
        ContentRiskFlags contentFlags = new ContentRiskFlags();
        DnsAnalysisResult dnsAnalysisResult = dnsAnalysisService.analyze(domain);
        if (dnsAnalysisResult == null) {
            dnsAnalysisResult = new DnsAnalysisResult();
        }

        whoisResult = whoisAnalysisService.analyze(domain);
        whoisDetails = whoisResult.getWhoisDetails();
        domainAgeDays = domainAgeCalculator.domainAgeInDays(
            whoisDetails == null ? null : whoisDetails.getCreationDate());
        sslResult = sslAnalysisService.inspect(domain);
        sslRiskFlags = sslAnalysisService.evaluateRisk(sslResult);
        contentFlags = contentAnalysisService.scan(domain, snapshot.getBrandKeyword());
        logger.info("Signals collected for {}: domainAgeDays={}, mxPresent={}, suspiciousHostingCountry={}",
            domain, domainAgeDays, dnsAnalysisResult.isMxPresent(), dnsAnalysisResult.isSuspiciousHostingCountry());

        RiskScoreInput input = new RiskScoreInput();
        input.setSimilarityScore(discoveredDomain.getSimilarityScore() == null ? 0 : discoveredDomain.getSimilarityScore().intValue());
        input.setDomainAgeDays(domainAgeDays);
        input.setWhoisDomainCount(whoisResult == null ? 0 : whoisResult.getAssociatedDomainCount());
        input.setRegistrarRiskLevel(registrarRisk);
        input.setSslRiskFlags(sslRiskFlags);
        input.setContentRiskFlags(contentFlags);
        input.setMxPresent(dnsAnalysisResult.isMxPresent());
        input.setSuspiciousHostingCountry(dnsAnalysisResult.isSuspiciousHostingCountry());
        input.setRegisteredDomain(true);
        input.setSslPresent(sslResult != null);
        input.setOfficialSubdomain(isOfficialSubdomain(snapshot, discoveredDomain));
        input.setApprovedRegistrar(isApprovedRegistrar(snapshot, discoveredDomain));
        input.setApprovedEmailProvider(isApprovedEmailProvider(snapshot, dnsAnalysisResult));

        RiskScoreResult scoreResult = scoringService.score(input);
        updateDiscoveredDomain(discoveredDomain, domainAgeDays, scoreResult);
        threatService.persistThreat(domain, scoreResult, whoisDetails,
            whoisDetails == null ? null : whoisDetails.getCreationDate(),
            input.getSimilarityScore(), sslResult, contentFlags, dnsAnalysisResult, input);

        logger.info("Risk score for {} is {} ({})", domain, scoreResult.getFinalScore(), scoreResult.getRiskLevel());
        return scoreResult;
    }

    private boolean isOfficialSubdomain(BrandSnapshot snapshot, DiscoveredDomainEntity discoveredDomain) {
        if (snapshot.getOfficialSubdomains() == null) {
            return false;
        }
        return snapshot.getOfficialSubdomains().stream()
            .anyMatch(subdomain -> subdomain != null && subdomain.equalsIgnoreCase(discoveredDomain.getDomainName()));
    }

    private boolean isApprovedRegistrar(BrandSnapshot snapshot, DiscoveredDomainEntity discoveredDomain) {
        if (snapshot.getApprovedRegistrars() == null || discoveredDomain.getRegistrar() == null) {
            return false;
        }
        return snapshot.getApprovedRegistrars().stream()
            .anyMatch(registrar -> registrar != null && registrar.equalsIgnoreCase(discoveredDomain.getRegistrar()));
    }

    private boolean isApprovedEmailProvider(BrandSnapshot snapshot, DnsAnalysisResult dnsAnalysisResult) {
        if (snapshot.getApprovedEmailProviders() == null || dnsAnalysisResult == null || !dnsAnalysisResult.isMxPresent()) {
            return false;
        }
        List<String> mxHosts = dnsAnalysisResult.getMxHosts();
        if (mxHosts == null || mxHosts.isEmpty()) {
            return false;
        }
        for (String mxHost : mxHosts) {
            String host = mxHost.toLowerCase(Locale.ROOT);
            for (String approved : snapshot.getApprovedEmailProviders()) {
                if (approved == null || approved.isBlank()) {
                    continue;
                }
                String approvedHost = approved.toLowerCase(Locale.ROOT);
                if (host.endsWith(approvedHost)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateDiscoveredDomain(DiscoveredDomainEntity discoveredDomain, int domainAgeDays, RiskScoreResult scoreResult) {
        discoveredDomain.setDomainAgeDays(domainAgeDays >= 0 ? domainAgeDays : null);
        discoveredDomain.setRiskScore(scoreResult.getFinalScore());
        discoveredDomain.setRiskLevel(scoreResult.getRiskLevel() == null ? RiskLevel.LOW : scoreResult.getRiskLevel());
        discoveredDomain.setLastChecked(Instant.now());
        discoveredDomainRepository.save(discoveredDomain);
    }
}
