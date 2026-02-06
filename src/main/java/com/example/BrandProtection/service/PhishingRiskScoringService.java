package com.example.BrandProtection.service;

import com.example.BrandProtection.domain.RiskLevel;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PhishingRiskScoringService {
    public RiskScoreResult score(RiskScoreInput input) {
        if (input.isOfficialSubdomain()) {
            RiskScoreResult result = new RiskScoreResult();
            result.setFinalScore(0);
            result.setRiskLevel(RiskLevel.LOW);
            result.setBreakdown(Map.of("officialSubdomain", -100));
            return result;
        }

        Map<String, Integer> breakdown = new LinkedHashMap<>();

        int similarityScore = scoreSimilarity(input.getSimilarityScore());
        breakdown.put("similarity", similarityScore);

        int registrationScore = input.isRegisteredDomain() ? 20 : 0;
        breakdown.put("registered", registrationScore);

        int domainAgeScore = scoreDomainAge(input.getDomainAgeDays());
        breakdown.put("domainAge", domainAgeScore);

        int whoisScore = scoreWhois(input.getWhoisDomainCount());
        breakdown.put("whoisCorrelation", whoisScore);

        int registrarScore = scoreRegistrar(input.getRegistrarRiskLevel());
        breakdown.put("registrarRisk", registrarScore);

        int sslScore = scoreSsl(input.getSslRiskFlags());
        breakdown.put("sslRisk", sslScore);

        int contentScore = scoreContent(input.getContentRiskFlags());
        breakdown.put("contentRisk", contentScore);

        int mxScore = input.isMxPresent() ? 20 : 0;
        breakdown.put("mxRisk", mxScore);

        int sslPresenceScore = input.isSslPresent() ? 10 : 0;
        breakdown.put("sslPresence", sslPresenceScore);

        int hostingScore = input.isSuspiciousHostingCountry() ? 10 : 0;
        breakdown.put("hostingRisk", hostingScore);

        int allowlistReduction = 0;
        if (input.isApprovedRegistrar()) {
            allowlistReduction -= 10;
        }
        if (input.isApprovedEmailProvider()) {
            allowlistReduction -= 10;
        }
        breakdown.put("allowlistReduction", allowlistReduction);

        int total = similarityScore + registrationScore + domainAgeScore + whoisScore + registrarScore
            + sslScore + contentScore + mxScore + sslPresenceScore + hostingScore + allowlistReduction;

        RiskScoreResult result = new RiskScoreResult();
        result.setFinalScore(Math.max(0, Math.min(100, total)));
        result.setRiskLevel(determineRiskLevel(result.getFinalScore()));
        result.setBreakdown(breakdown);
        return result;
    }

    // Similarity: >90% -> 40, 80-90 -> 25, 70-80 -> 10, <70 -> 0
    private int scoreSimilarity(int similarityPercent) {
        if (similarityPercent >= 90) {
            return 40;
        }
        if (similarityPercent >= 80) {
            return 25;
        }
        if (similarityPercent >= 70) {
            return 10;
        }
        return 0;
    }

    // Domain age: <30 days -> 20
    private int scoreDomainAge(int domainAgeDays) {
        if (domainAgeDays < 0) {
            return 0;
        }
        if (domainAgeDays < 30) {
            return 20;
        }
        return 0;
    }

    // WHOIS: >20 -> 20, 10-20 -> 15, 3-9 -> 10, no info -> 5
    private int scoreWhois(int whoisDomainCount) {
        if (whoisDomainCount <= 0) {
            return 5;
        }
        if (whoisDomainCount > 20) {
            return 20;
        }
        if (whoisDomainCount >= 10) {
            return 15;
        }
        if (whoisDomainCount >= 3) {
            return 10;
        }
        return 0;
    }

    // Registrar: high abuse -> 10, low-cost -> 5, reputable -> 0
    private int scoreRegistrar(RegistrarRiskLevel registrarRiskLevel) {
        if (registrarRiskLevel == null) {
            return 5;
        }
        return switch (registrarRiskLevel) {
            case HIGH_ABUSE -> 10;
            case LOW_COST -> 5;
            case REPUTABLE -> 0;
        };
    }

    // SSL risk: issued <3d -> 10, validity <30d -> 7, unknown issuer -> 5
    private int scoreSsl(SslRiskFlags flags) {
        if (flags == null) {
            return 0;
        }
        if (flags.isIssuedRecently()) {
            return 10;
        }
        if (flags.isShortValidity()) {
            return 7;
        }
        if (flags.isUnknownIssuer()) {
            return 5;
        }
        return 0;
    }

    // Content: login form -> 40, brand keywords -> 20, redirect chain -> 10
    private int scoreContent(ContentRiskFlags flags) {
        if (flags == null) {
            return 0;
        }
        if (flags.isLoginFormDetected()) {
            return 40;
        }
        if (flags.isBrandKeywordsPresent()) {
            return 20;
        }
        if (flags.isRedirectChain()) {
            return 10;
        }
        return 0;
    }

    private RiskLevel determineRiskLevel(int score) {
        if (score >= 51) {
            return RiskLevel.HIGH;
        }
        if (score >= 21) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
