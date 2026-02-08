package com.example.BrandProtection.service;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SslAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(SslAnalysisService.class);

    @Cacheable(cacheNames = "sslInspection", key = "#domain")
    public SslInspectionResult inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            return null;
        }
        logger.info("SSL inspection started for {}.", domain);
        try (SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(domain, 443)) {
            sslSocket.startHandshake();
            X509Certificate certificate = (X509Certificate) sslSocket.getSession().getPeerCertificates()[0];
            SslInspectionResult result = new SslInspectionResult();
            result.setIssuer(certificate.getIssuerX500Principal().getName());
            result.setValidFrom(certificate.getNotBefore().toInstant());
            result.setValidTo(certificate.getNotAfter().toInstant());
            result.setSelfSigned(isSelfSigned(certificate));
            logger.info("SSL inspection completed for {}.", domain);
            return result;
        } catch (IOException ex) {
            logger.info("SSL inspection failed for {}: {}", domain, ex.getMessage());
            return null;
        }
    }

    public SslRiskFlags evaluateRisk(SslInspectionResult result) {
        SslRiskFlags flags = new SslRiskFlags();
        if (result == null) {
            flags.setUnknownIssuer(true);
            return flags;
        }
        Instant now = Instant.now();
        if (result.getValidFrom() != null) {
            flags.setIssuedRecently(Duration.between(result.getValidFrom(), now).toDays() < 3);
        }
        if (result.getValidFrom() != null && result.getValidTo() != null) {
            flags.setShortValidity(Duration.between(result.getValidFrom(), result.getValidTo()).toDays() < 30);
        }
        if (result.getIssuer() != null) {
            flags.setUnknownIssuer(result.getIssuer().toLowerCase(Locale.ROOT).contains("unknown"));
        }
        return flags;
    }

    private boolean isSelfSigned(X509Certificate certificate) {
        return certificate.getIssuerX500Principal().equals(certificate.getSubjectX500Principal());
    }
}
