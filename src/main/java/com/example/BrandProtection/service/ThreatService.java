package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.RiskLevel;
import com.example.BrandProtection.domain.ThreatEntity;
import com.example.BrandProtection.domain.ThreatRepository;
import com.example.BrandProtection.domain.ThreatSeverity;
import com.example.BrandProtection.domain.ThreatType;
import com.example.BrandProtection.domainiq.dto.WhoisDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ThreatService {
    private static final Logger logger = LoggerFactory.getLogger(ThreatService.class);

    private final ThreatRepository threatRepository;
    private final ObjectMapper objectMapper;

    public ThreatService(ThreatRepository threatRepository, ObjectMapper objectMapper) {
        this.threatRepository = threatRepository;
        this.objectMapper = objectMapper;
    }

    public void persistThreat(String domain, RiskScoreResult scoreResult, WhoisDetails whoisDetails,
                              String registrationDate, int similarityScore, SslInspectionResult sslResult,
                              ContentRiskFlags contentFlags, DnsAnalysisResult dnsAnalysisResult,
                              RiskScoreInput scoreInput) {
        ThreatEntity threat = new ThreatEntity();
        threat.setDomainName(domain);
        threat.setType(ThreatType.PHISHING);
        threat.setSeverity(mapSeverity(scoreResult.getRiskLevel()));
        threat.setDetectedAt(Instant.now());
        threat.setRiskScore(scoreResult.getFinalScore());
        threat.setEvidenceJson(serializeEvidence(whoisDetails, registrationDate, similarityScore, sslResult,
            contentFlags, dnsAnalysisResult, scoreInput));

        threatRepository.save(threat);
        logger.info("Persisted threat for domain {} with score {} and level {}",
            domain, scoreResult.getFinalScore(), scoreResult.getRiskLevel());
    }

    private String serializeEvidence(WhoisDetails whoisDetails, String registrationDate, int similarityScore,
                                     SslInspectionResult sslResult, ContentRiskFlags contentFlags,
                                     DnsAnalysisResult dnsAnalysisResult, RiskScoreInput scoreInput) {
        Map<String, Object> evidence = new HashMap<>();
        evidence.put("whois", whoisDetails);
        evidence.put("registrationDate", registrationDate);
        evidence.put("similarityScore", similarityScore);
        evidence.put("ssl", sslResult);
        evidence.put("contentFlags", contentFlags);
        evidence.put("dns", dnsAnalysisResult);
        if (scoreInput != null) {
            Map<String, Object> allowlist = new HashMap<>();
            allowlist.put("officialSubdomain", scoreInput.isOfficialSubdomain());
            allowlist.put("approvedRegistrar", scoreInput.isApprovedRegistrar());
            allowlist.put("approvedEmailProvider", scoreInput.isApprovedEmailProvider());
            evidence.put("allowlist", allowlist);
            evidence.put("mxPresent", scoreInput.isMxPresent());
            evidence.put("registeredDomain", scoreInput.isRegisteredDomain());
        }
        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"failed_to_serialize_evidence\"}";
        }
    }

    private ThreatSeverity mapSeverity(RiskLevel riskLevel) {
        if (riskLevel == null) {
            return ThreatSeverity.LOW;
        }
        return switch (riskLevel) {
            case HIGH -> ThreatSeverity.HIGH;
            case MEDIUM -> ThreatSeverity.MEDIUM;
            case LOW -> ThreatSeverity.LOW;
        };
    }
}
