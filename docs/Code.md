# Code Walkthrough (Full Project)

This document explains how the app works end to end, with pointers to the key files
and the flow from brand registration to threat persistence.

## 1) Project layout (what lives where)
- `src/main/java/com/example/BrandProtection/`
  - `DomainBrandProtectionApplication.java`: Spring Boot entry point, enables config properties, cache, async.
  - `config/`: security and shared config.
  - `api/`: REST controllers and DTOs.
  - `domain/`: JPA entities and repositories.
  - `domainiq/`: DomainIQ client + DTOs + config properties.
  - `service/`: discovery, analysis, scoring, orchestration.
- `src/main/resources/`
  - `application.properties`: common config (active profile).
  - `application-dev.properties`: local settings (DB, DomainIQ, security).
  - `application-prod.properties`: production settings (env-driven).
- `docs/`
  - `DATABASE_MODEL.md`: schema and relations.
  - `THREAT_DETECTION_FLOW.md`: high-level flow.
  - `POSTMAN_REQUESTS.md`: API examples.
  - `Problem.md`: issues and fixes.

## 2) Application bootstrapping
Entry point:
```11:16:src/main/java/com/example/BrandProtection/DomainBrandProtectionApplication.java
@SpringBootApplication
@EnableConfigurationProperties({DomainIqProperties.class, DomainDiscoveryProperties.class})
@EnableCaching
@EnableScheduling
@EnableAsync
public class DomainBrandProtectionApplication {
```
Notes:
- `DomainIqProperties` and `DomainDiscoveryProperties` load config keys.
- Async and caching are enabled globally.

## 3) API endpoints
Main controller:
```8:39:src/main/java/com/example/BrandProtection/api/BrandProtectionController.java
@PostMapping("/api/internal/brands")
public ResponseEntity<RegisterBrandResponse> registerBrand(@Valid @RequestBody RegisterBrandRequest request) { ... }

@GetMapping("/api/internal/brands/{id}/threats")
public ResponseEntity<ThreatResponse> getThreats(@PathVariable UUID id) { ... }

@PostMapping("/api/internal/brands/{id}/discover")
public ResponseEntity<Void> runDiscovery(@PathVariable UUID id) { ... }
```
What they do:
- `POST /api/internal/brands`: registers a brand and immediately triggers discovery.
- `GET /api/internal/brands/{id}/threats`: returns saved threats.
- `POST /api/internal/brands/{id}/discover`: manual discovery trigger.

## 4) Registration and immediate processing
Registration service:
```36:53:src/main/java/com/example/BrandProtection/service/ProtectedBrandService.java
public UUID registerBrand(RegisterBrandRequest request) {
    ProtectedDomainEntity entity = new ProtectedDomainEntity();
    entity.setBrandDomain(request.getBrandDomain());
    entity.setBrandKeyword(request.getBrandKeyword());
    entity.setBrandName(request.getBrandName());
    entity.setKeywords(request.getKeywords());
    entity.setOfficialSubdomains(request.getOfficialSubdomains());
    entity.setApprovedRegistrars(request.getApprovedRegistrars());
    entity.setApprovedEmailProviders(request.getApprovedEmailProviders());
    entity.setStatus(BrandStatus.ACTIVE);
    ProtectedDomainEntity saved = protectedDomainRepository.save(entity);
    monitoringOrchestrator.runForBrand(saved);
    return saved.getId();
}
```
Key idea:
- The full baseline is saved first, then discovery is triggered immediately.

## 5) Async orchestration (safe snapshot)
```56:72:src/main/java/com/example/BrandProtection/service/MonitoringOrchestrator.java
@Transactional
public void runForBrand(ProtectedDomainEntity brand) {
    BrandSnapshot snapshot = toSnapshot(brand);
    runForBrandAsync(snapshot, brand);
}

@Async
public void runForBrandAsync(BrandSnapshot snapshot, ProtectedDomainEntity brand) {
    List<DiscoveredDomainEntity> discoveredDomains = domainDiscoveryService.discover(snapshot, brand);
    for (DiscoveredDomainEntity discoveredDomain : discoveredDomains) {
        riskAssessmentService.assess(discoveredDomain, snapshot);
    }
}
```
Why snapshot:
- `BrandSnapshot` is built before async work to avoid lazy-loading errors.
- Async thread only sees plain DTO fields (thread-safe, predictable).

## 6) Discovery pipeline (DomainIQ + variations)
```44:87:src/main/java/com/example/BrandProtection/service/DomainDiscoveryService.java
public List<DiscoveredDomainEntity> discover(BrandSnapshot snapshot, ProtectedDomainEntity protectedDomain) {
    Set<String> candidateDomains = new LinkedHashSet<>();
    List<DomainSearchResult> results = domainIqClient.searchDomainsByKeyword(snapshot.getBrandKeyword() + "*");
    Set<String> domainIqDomains = results.stream()
        .map(DomainSearchResult::getDomain)
        .filter(domain -> domain != null && !domain.isBlank())
        .collect(Collectors.toCollection(LinkedHashSet::new));
    candidateDomains.addAll(domainIqDomains);

    Set<String> generated = variationGenerator.generate(snapshot.getBrandDomain(), snapshot.getKeywords());
    candidateDomains.addAll(generated);

    persistSimilarDomains(candidateDomains, domainIqDomains, generated, protectedDomain);

    for (String domain : candidateDomains) {
        if (!domainIqDomains.contains(domain) && !isRegisteredDomain(domain)) { continue; }
        int similarity = similarityService.similarityPercent(domain, snapshot.getBrandKeyword());
        if (similarity < SIMILARITY_THRESHOLD) { continue; }
        // build entity + save later
    }
    return discoveredDomainRepository.saveAll(discovered);
}
```
Key steps:
- DomainIQ search returns real registered domains.
- Generator produces typos/homoglyphs/TLD abuse candidates.
- DNS existence filter keeps only real domains.
- Similarity filter removes weak matches.
- Results are stored in `suspicious_domains_discovered`.
- All candidates (DomainIQ + generated) are saved to `similar_domains`.
- Candidates are saved in a separate transaction so they persist even if later DNS calls fail.

## 7) Candidate limits (load control)
To avoid timeouts on large searches, discovery limits are configurable:
- `discovery.max-domainiq-results`
- `discovery.max-generated`
- `discovery.max-candidates`

These are applied before DNS checks to reduce outbound load.

## 8) DNS checks
```101:109:src/main/java/com/example/BrandProtection/service/DomainDiscoveryService.java
private boolean isRegisteredDomain(String domain) {
    DnsDetails dnsDetails = domainIqClient.getDnsHistory(domain);
    if (dnsDetails == null) { return false; }
    return (dnsDetails.getaRecords() != null && !dnsDetails.getaRecords().isEmpty())
        || (dnsDetails.getMxRecords() != null && !dnsDetails.getMxRecords().isEmpty())
        || (dnsDetails.getNsRecords() != null && !dnsDetails.getNsRecords().isEmpty());
}
```

## 9) Risk assessment flow
File: `src/main/java/com/example/BrandProtection/service/DomainRiskAssessmentService.java`
- WHOIS, SSL, content, DNS, registrar analysis.
- Builds `RiskScoreInput`.
- Calls `PhishingRiskScoringService` to compute final score and level.
- Saves to `ThreatEntity` and updates `DiscoveredDomainEntity`.

## 10) Threat persistence
File: `src/main/java/com/example/BrandProtection/service/ThreatService.java`
- Saves threats for LOW, MEDIUM, HIGH.
- Stores risk score and evidence JSON.

## 11) DomainIQ integration
File: `src/main/java/com/example/BrandProtection/domainiq/DomainIqClient.java`
Features:
- Uses query-param format: `key`, `service`, `keyword`, `q`, `output_mode`.
- Configurable timeouts and retries.
- Retry + backoff on timeout/network or 5xx.
- Concurrency limiter to cap parallel DomainIQ calls.
- Simple circuit breaker to pause calls when repeated failures occur.
- Response size limit for large payloads.

Current API format:
```
GET https://www.domainiq.com/api?key=<API_KEY>&service=domain_search&keyword=flipkart&condition=contains&output_mode=json
GET https://www.domainiq.com/api?key=<API_KEY>&service=whois&domain=flipkart.com&output_mode=json
GET https://www.domainiq.com/api?key=<API_KEY>&service=dns&q=flipkart.com&output_mode=json
```

## 12) Configuration keys (quick reference)
`application-dev.properties` and `application-prod.properties`:
- `domainiq.base-url`, `domainiq.api-key`
- `domainiq.timeout-ms`, `domainiq.retry-max-attempts`, `domainiq.retry-backoff-ms`
- `domainiq.max-concurrent-requests`
- `domainiq.circuit-breaker-enabled`, `domainiq.circuit-breaker-failure-threshold`,
  `domainiq.circuit-breaker-open-ms`
- `domainiq.search-service`, `domainiq.whois-service`, `domainiq.dns-service`
- `spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=CHAR` (UUIDs stored as readable `CHAR(36)`)
- `domainiq.output-mode`, `domainiq.max-response-size-bytes`
- `discovery.max-domainiq-results`, `discovery.max-generated`, `discovery.max-candidates`

## 13) Data model summary (entities)
Files in `src/main/java/com/example/BrandProtection/domain/`:
- `ProtectedDomainEntity` maps to `registered_domains`
- `DiscoveredDomainEntity` maps to `suspicious_domains_discovered`
- `SimilarDomainEntity` maps to `similar_domains`
- `ThreatEntity` stores risk details and evidence
- Element-collection tables store:
  - `keywords`
  - `official_subdomains`
  - `approved_registrars`
  - `approved_email_providers`

For full details see `docs/DATABASE_MODEL.md`.

## 14) Security (local vs prod)
File: `src/main/java/com/example/BrandProtection/config/SecurityConfig.java`
- `/api/internal/**` is protected by JWT scope.
- Dev can allow all with `security.internal.permit-all=true`.

## 15) How to explain the app (one paragraph)
The app registers a brand baseline (domain, keywords, allowlists), then immediately
runs an async discovery job. It uses DomainIQ search plus generated variations to
build candidates, filters them by DNS existence and similarity, then enriches each
domain with WHOIS, SSL, DNS, and content signals to score phishing risk. Every
discovered domain is saved, and threats are persisted with LOW/MEDIUM/HIGH plus
evidence so analysts can review the results.
