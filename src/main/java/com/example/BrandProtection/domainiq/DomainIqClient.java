package com.example.BrandProtection.domainiq;

import com.example.BrandProtection.domainiq.dto.DnsDetails;
import com.example.BrandProtection.domainiq.dto.DnsHistoryResponse;
import com.example.BrandProtection.domainiq.dto.DomainSearchItem;
import com.example.BrandProtection.domainiq.dto.DomainSearchResponse;
import com.example.BrandProtection.domainiq.dto.DomainSearchResult;
import com.example.BrandProtection.domainiq.dto.WhoisDetails;
import com.example.BrandProtection.domainiq.dto.WhoisResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Service
public class DomainIqClient {
    private static final Logger logger = LoggerFactory.getLogger(DomainIqClient.class);
    private static final int MAX_DNS_RECORDS = 200;

    private final DomainIqRateLimiter rateLimiter;
    private final WebClient webClient;

    public DomainIqClient(DomainIqProperties properties) {
        this.rateLimiter = new DomainIqRateLimiter(properties.getRateLimitPerMinute());
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(properties.getTimeoutMs()));

        this.webClient = WebClient.builder()
            .baseUrl(properties.getBaseUrl())
            .defaultHeader("X-API-Key", properties.getApiKey())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    @Cacheable(cacheNames = "domainiqSearch", key = "#keyword")
    public List<DomainSearchResult> searchDomainsByKeyword(String keyword) {
        rateLimiter.acquire();
        DomainSearchResponse response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/domains/search")
                .queryParam("query", keyword)
                .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, clientResponse ->
                clientResponse.bodyToMono(String.class)
                    .defaultIfEmpty("DomainIQ error")
                    .map(message -> new DomainIqException("DomainIQ search failed: " + message)))
            .bodyToMono(DomainSearchResponse.class)
            .retryWhen(Retry.max(1).filter(DomainIqClient::isRetryable))
            .block();

        if (response == null || response.getDomains() == null) {
            return Collections.emptyList();
        }

        return response.getDomains().stream()
            .filter(Objects::nonNull)
            .map(this::toSearchResult)
            .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "domainiqWhois", key = "#domain")
    public WhoisDetails getWhoisInfo(String domain) {
        rateLimiter.acquire();
        WhoisResponse response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/whois")
                .queryParam("domain", domain)
                .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, clientResponse ->
                clientResponse.bodyToMono(String.class)
                    .defaultIfEmpty("DomainIQ error")
                    .map(message -> new DomainIqException("DomainIQ WHOIS failed: " + message)))
            .bodyToMono(WhoisResponse.class)
            .retryWhen(Retry.max(1).filter(DomainIqClient::isRetryable))
            .block();

        return normalizeWhois(response == null ? null : response.getWhois());
    }

    @Cacheable(cacheNames = "domainiqDns", key = "#domain")
    public DnsDetails getDnsHistory(String domain) {
        rateLimiter.acquire();
        DnsHistoryResponse response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/dns/history")
                .queryParam("domain", domain)
                .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, clientResponse ->
                clientResponse.bodyToMono(String.class)
                    .defaultIfEmpty("DomainIQ error")
                    .map(message -> new DomainIqException("DomainIQ DNS history failed: " + message)))
            .bodyToMono(DnsHistoryResponse.class)
            .retryWhen(Retry.max(1).filter(DomainIqClient::isRetryable))
            .block();

        DnsDetails dns = response == null ? null : response.getDns();
        if (dns == null) {
            return null;
        }

        if (dns.getaRecords() != null && dns.getaRecords().size() > MAX_DNS_RECORDS) {
            logger.info("Truncating DNS A records for {} from {} to {}.", domain, dns.getaRecords().size(), MAX_DNS_RECORDS);
            dns.setaRecords(dns.getaRecords().subList(0, MAX_DNS_RECORDS));
        }
        if (dns.getNsRecords() != null && dns.getNsRecords().size() > MAX_DNS_RECORDS) {
            logger.info("Truncating DNS NS records for {} from {} to {}.", domain, dns.getNsRecords().size(), MAX_DNS_RECORDS);
            dns.setNsRecords(dns.getNsRecords().subList(0, MAX_DNS_RECORDS));
        }
        if (dns.getMxRecords() != null && dns.getMxRecords().size() > MAX_DNS_RECORDS) {
            logger.info("Truncating DNS MX records for {} from {} to {}.", domain, dns.getMxRecords().size(), MAX_DNS_RECORDS);
            dns.setMxRecords(dns.getMxRecords().subList(0, MAX_DNS_RECORDS));
        }

        return dns;
    }

    private DomainSearchResult toSearchResult(DomainSearchItem item) {
        return new DomainSearchResult(
            item.getDomain(),
            item.getFirstSeen(),
            item.getRegistrar(),
            item.getCountry());
    }

    private WhoisDetails normalizeWhois(WhoisDetails details) {
        if (details == null) {
            return null;
        }
        details.setRegistrantName(normalizeField(details.getRegistrantName()));
        details.setRegistrantEmail(normalizeField(details.getRegistrantEmail()));
        details.setRegistrantOrg(normalizeField(details.getRegistrantOrg()));
        return details;
    }

    private String normalizeField(String value) {
        if (value == null) {
            return "UNKNOWN";
        }
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.equalsIgnoreCase("redacted") || normalized.equalsIgnoreCase("privacy")) {
            return "REDACTED";
        }
        return normalized;
    }

    private static boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError();
        }
        return false;
    }
}
