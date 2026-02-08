package com.example.BrandProtection.domainiq;

import com.example.BrandProtection.domainiq.dto.DnsDetails;
import com.example.BrandProtection.domainiq.dto.DnsHistoryResponse;
import com.example.BrandProtection.domainiq.dto.DomainSearchItem;
import com.example.BrandProtection.domainiq.dto.DomainSearchResponse;
import com.example.BrandProtection.domainiq.dto.DomainSearchResult;
import com.example.BrandProtection.domainiq.dto.WhoisDetails;
import com.example.BrandProtection.domainiq.dto.WhoisResponse;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.Exceptions;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import io.netty.handler.timeout.ReadTimeoutException;

@Service
public class DomainIqClient {
    private static final Logger logger = LoggerFactory.getLogger(DomainIqClient.class);
    private static final int MAX_DNS_RECORDS = 200;

    private final DomainIqRateLimiter rateLimiter;
    private final WebClient webClient;
    private final DomainIqProperties properties;
    private final Semaphore concurrencyLimiter;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong circuitOpenUntilMs = new AtomicLong(0L);

    public DomainIqClient(DomainIqProperties properties) {
        this.properties = properties;
        this.rateLimiter = new DomainIqRateLimiter(properties.getRateLimitPerMinute());
        this.concurrencyLimiter = new Semaphore(properties.getMaxConcurrentRequests(), true);
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(properties.getTimeoutMs()));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(properties.getMaxResponseSizeBytes()))
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private Retry retrySpec() {
        int maxAttempts = properties.getRetryMaxAttempts();
        if (maxAttempts <= 0) {
            return Retry.max(0);
        }
        return Retry.backoff(maxAttempts, Duration.ofMillis(properties.getRetryBackoffMs()))
                .filter(DomainIqClient::isRetryable)
                .doBeforeRetry(signal -> logger.warn("Retrying DomainIQ call after error: {}",
                        Exceptions.unwrap(signal.failure()).toString()));
    }

    @Cacheable(cacheNames = "domainiqSearch", key = "#keyword")
    public List<DomainSearchResult> searchDomainsByKeyword(String keyword) {
        DomainSearchResponse response = executeWithLimit(() -> {
            rateLimiter.acquire();
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", properties.getApiKey())
                            .queryParam("service", properties.getSearchService())
                            .queryParam("keyword", keyword)
                            .queryParam("condition", properties.getSearchCondition())
                            .queryParam("output_mode", properties.getOutputMode())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("DomainIQ error")
                            .map(message -> new DomainIqException("DomainIQ search failed: " + message)))
                    .bodyToMono(DomainSearchResponse.class)
                    .retryWhen(retrySpec())
                    .block();
        });

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
        WhoisResponse response = executeWithLimit(() -> {
            rateLimiter.acquire();
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", properties.getApiKey())
                            .queryParam("service", properties.getWhoisService())
                            .queryParam("domain", domain)
                            .queryParam("output_mode", properties.getOutputMode())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("DomainIQ error")
                            .map(message -> new DomainIqException("DomainIQ WHOIS failed: " + message)))
                    .bodyToMono(WhoisResponse.class)
                    .retryWhen(retrySpec())
                    .block();
        });

        return normalizeWhois(response == null ? null : response.getWhois());
    }

    @Cacheable(cacheNames = "domainiqDns", key = "#domain")
    public DnsDetails getDnsHistory(String domain) {
        DnsHistoryResponse response = executeWithLimit(() -> {
            rateLimiter.acquire();
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", properties.getApiKey())
                            .queryParam("service", properties.getDnsService())
                            .queryParam("q", domain)
                            .queryParam("output_mode", properties.getOutputMode())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("DomainIQ error")
                            .map(message -> new DomainIqException("DomainIQ DNS history failed: " + message)))
                    .bodyToMono(DnsHistoryResponse.class)
                    .retryWhen(retrySpec())
                    .block();
        });

        DnsDetails dns = response == null ? null : response.getDns();
        if (dns == null) {
            return null;
        }

        if (dns.getaRecords() != null && dns.getaRecords().size() > MAX_DNS_RECORDS) {
            logger.info("Truncating DNS A records for {} from {} to {}.", domain, dns.getaRecords().size(),
                    MAX_DNS_RECORDS);
            dns.setaRecords(dns.getaRecords().subList(0, MAX_DNS_RECORDS));
        }
        if (dns.getNsRecords() != null && dns.getNsRecords().size() > MAX_DNS_RECORDS) {
            logger.info("Truncating DNS NS records for {} from {} to {}.", domain, dns.getNsRecords().size(),
                    MAX_DNS_RECORDS);
            dns.setNsRecords(dns.getNsRecords().subList(0, MAX_DNS_RECORDS));
        }
        if (dns.getMxRecords() != null && dns.getMxRecords().size() > MAX_DNS_RECORDS) {
            logger.info("Truncating DNS MX records for {} from {} to {}.", domain, dns.getMxRecords().size(),
                    MAX_DNS_RECORDS);
            dns.setMxRecords(dns.getMxRecords().subList(0, MAX_DNS_RECORDS));
        }

        return dns;
    }

    private DomainSearchResult toSearchResult(DomainSearchItem item) {
        String registrationDate = item.getFirstSeen();
        if (registrationDate == null || registrationDate.isBlank()) {
            registrationDate = item.getCreateDate();
        }
        return new DomainSearchResult(
                item.getDomain(),
                registrationDate,
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
        Throwable unwrapped = Exceptions.unwrap(throwable);
        if (unwrapped instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError();
        }
        if (unwrapped instanceof WebClientRequestException) {
            return true;
        }
        if (unwrapped instanceof ReadTimeoutException) {
            return true;
        }
        return false;
    }

    private <T> T executeWithLimit(java.util.function.Supplier<T> supplier) {
        if (properties.getCircuitBreakerEnabled() && isCircuitOpen()) {
            throw new DomainIqException("DomainIQ circuit breaker is open; skipping call.");
        }
        acquirePermit();
        try {
            T result = supplier.get();
            onCallSuccess();
            return result;
        } catch (RuntimeException ex) {
            onCallFailure(ex);
            throw ex;
        } finally {
            concurrencyLimiter.release();
        }
    }

    private void acquirePermit() {
        try {
            concurrencyLimiter.acquire();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new DomainIqException("Interrupted while waiting for DomainIQ permit.");
        }
    }

    private boolean isCircuitOpen() {
        long openUntil = circuitOpenUntilMs.get();
        if (openUntil <= 0) {
            return false;
        }
        if (System.currentTimeMillis() < openUntil) {
            return true;
        }
        circuitOpenUntilMs.set(0L);
        failureCount.set(0);
        return false;
    }

    private void onCallSuccess() {
        failureCount.set(0);
    }

    private void onCallFailure(Throwable ex) {
        if (!properties.getCircuitBreakerEnabled()) {
            return;
        }
        Throwable unwrapped = Exceptions.unwrap(ex);
        if (!isRetryable(unwrapped)) {
            return;
        }
        int failures = failureCount.incrementAndGet();
        if (failures >= properties.getCircuitBreakerFailureThreshold()) {
            long openUntil = System.currentTimeMillis() + properties.getCircuitBreakerOpenMs();
            circuitOpenUntilMs.set(openUntil);
            logger.warn("DomainIQ circuit breaker opened for {} ms after {} failures.",
                properties.getCircuitBreakerOpenMs(), failures);
        }
    }
}
