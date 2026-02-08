package com.example.BrandProtection.domainiq;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "domainiq")
public class DomainIqProperties {
    @NotBlank
    private String baseUrl;

    @NotBlank
    private String apiKey;

    @NotNull
    @Min(1000L)
    private Integer timeoutMs;

    @NotNull
    @Min(1L)
    private Integer rateLimitPerMinute;

    @NotBlank
    private String searchService;

    @NotBlank
    private String whoisService;

    @NotBlank
    private String dnsService;

    @NotBlank
    private String searchCondition;

    @NotBlank
    private String outputMode;

    @NotNull
    @Min(262144L)
    private Integer maxResponseSizeBytes;

    @NotNull
    @Min(0L)
    private Integer retryMaxAttempts;

    @NotNull
    @Min(0L)
    private Integer retryBackoffMs;

    @NotNull
    @Min(1L)
    private Integer maxConcurrentRequests;

    @NotNull
    private Boolean circuitBreakerEnabled;

    @NotNull
    @Min(1L)
    private Integer circuitBreakerFailureThreshold;

    @NotNull
    @Min(1000L)
    private Integer circuitBreakerOpenMs;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Integer getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(Integer rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public String getSearchService() {
        return searchService;
    }

    public void setSearchService(String searchService) {
        this.searchService = searchService;
    }

    public String getWhoisService() {
        return whoisService;
    }

    public void setWhoisService(String whoisService) {
        this.whoisService = whoisService;
    }

    public String getDnsService() {
        return dnsService;
    }

    public void setDnsService(String dnsService) {
        this.dnsService = dnsService;
    }

    public String getSearchCondition() {
        return searchCondition;
    }

    public void setSearchCondition(String searchCondition) {
        this.searchCondition = searchCondition;
    }

    public String getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(String outputMode) {
        this.outputMode = outputMode;
    }

    public Integer getMaxResponseSizeBytes() {
        return maxResponseSizeBytes;
    }

    public void setMaxResponseSizeBytes(Integer maxResponseSizeBytes) {
        this.maxResponseSizeBytes = maxResponseSizeBytes;
    }

    public Integer getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    public void setRetryMaxAttempts(Integer retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }

    public Integer getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public void setRetryBackoffMs(Integer retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }

    public Integer getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    public void setMaxConcurrentRequests(Integer maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    public Boolean getCircuitBreakerEnabled() {
        return circuitBreakerEnabled;
    }

    public void setCircuitBreakerEnabled(Boolean circuitBreakerEnabled) {
        this.circuitBreakerEnabled = circuitBreakerEnabled;
    }

    public Integer getCircuitBreakerFailureThreshold() {
        return circuitBreakerFailureThreshold;
    }

    public void setCircuitBreakerFailureThreshold(Integer circuitBreakerFailureThreshold) {
        this.circuitBreakerFailureThreshold = circuitBreakerFailureThreshold;
    }

    public Integer getCircuitBreakerOpenMs() {
        return circuitBreakerOpenMs;
    }

    public void setCircuitBreakerOpenMs(Integer circuitBreakerOpenMs) {
        this.circuitBreakerOpenMs = circuitBreakerOpenMs;
    }
}
