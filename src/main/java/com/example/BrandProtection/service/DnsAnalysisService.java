package com.example.BrandProtection.service;

import com.example.BrandProtection.domainiq.DomainIqClient;
import com.example.BrandProtection.domainiq.dto.DnsDetails;
import com.example.BrandProtection.domainiq.dto.DnsRecord;
import com.example.BrandProtection.domainiq.dto.MxRecord;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DnsAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(DnsAnalysisService.class);
    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("RU", "CN", "IR", "KP");

    private final DomainIqClient domainIqClient;

    public DnsAnalysisService(DomainIqClient domainIqClient) {
        this.domainIqClient = domainIqClient;
    }

    public DnsAnalysisResult analyze(String domain) {
        DnsAnalysisResult result = new DnsAnalysisResult();
        DnsDetails dnsDetails = domainIqClient.getDnsHistory(domain);
        if (dnsDetails == null) {
            return result;
        }

        if (dnsDetails.getMxRecords() != null && !dnsDetails.getMxRecords().isEmpty()) {
            result.setMxPresent(true);
            List<String> mxHosts = dnsDetails.getMxRecords().stream()
                .map(MxRecord::getHost)
                .filter(host -> host != null && !host.isBlank())
                .collect(Collectors.toList());
            result.setMxHosts(mxHosts);
        }

        List<DnsRecord> aRecords = dnsDetails.getaRecords();
        if (aRecords != null) {
            for (DnsRecord record : aRecords) {
                if (record.getCountry() != null &&
                    HIGH_RISK_COUNTRIES.contains(record.getCountry().toUpperCase(Locale.ROOT))) {
                    result.setSuspiciousHostingCountry(true);
                    break;
                }
            }
        }

        logger.info("DNS analysis for {}: mxPresent={}, suspiciousHostingCountry={}",
            domain, result.isMxPresent(), result.isSuspiciousHostingCountry());
        return result;
    }
}
