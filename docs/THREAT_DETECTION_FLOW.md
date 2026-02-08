# Threat Detection Flow (End-to-End)

This document explains how threats are detected from the moment a user registers a brand to the final threat output. It maps every controller and service involved in the pipeline.

## 1) User Registration Entry Point

### Controller
- `BrandProtectionController`
  - **Endpoint**: `POST /api/internal/brands`
  - **DTO**: `RegisterBrandRequest`
  - **Purpose**: Accepts a brand for monitoring with baseline allowlists:
    - brandName, brandDomain, brandKeyword
    - keywords
    - officialSubdomains
    - approvedRegistrars
    - approvedEmailProviders

### Service
- `ProtectedBrandService.registerBrand(...)`
  - Creates `ProtectedDomainEntity`
  - Sets `status=ACTIVE`, `createdAt=now`
  - Saves to database via `ProtectedDomainRepository`
  - Returns the new brand UUID

## 2) Immediate Monitoring Starts the Pipeline

### Trigger
- `ProtectedBrandService.registerBrand(...)`
  - After saving the brand, it triggers `MonitoringOrchestrator.runForBrand(...)`

## 3) Orchestration Layer

### Service
- `MonitoringOrchestrator`
  - Fetches active brands from `ProtectedDomainRepository`
  - For each brand:
    1. Discover candidate domains (DomainIQ + generated variations)
    2. Run risk assessment on each discovered domain

## 4) Discovery (New Suspicious Domains)

### Service
- `DomainDiscoveryService`
  - Builds query: `brandKeyword + "*"`.
  - Calls DomainIQ via `DomainIqClient.searchDomainsByKeyword(...)`.
  - Generates typo/variation candidates via `DomainVariationGenerator`.
  - Filters generated candidates by DNS existence (A/MX/NS).
  - For each domain:
    - Computes similarity via `SimilarityService`.
    - Filters out low similarity (threshold = 70).
  - Persists results as `DiscoveredDomainEntity` in `DiscoveredDomainRepository`
    (table `suspicious_domains_discovered`).

### DomainIQ Client
- `DomainIqClient`
  - Uses `WebClient` with API key
  - Rate-limited by `DomainIqRateLimiter` (60/min)
  - DTO mapping for:
    - Search (`DomainSearchResponse`) 
    - WHOIS (`WhoisResponse`)
    - DNS history (`DnsHistoryResponse`)
  - Caches responses with Caffeine

## 5) Risk Assessment (Core Threat Logic)

### Service
- `DomainRiskAssessmentService`
  - Inputs: `DiscoveredDomainEntity`, `brandKeyword`
  - Builds `RiskScoreInput` using these signals:
    1. **Similarity** (from discovery)
    2. **Domain age**
    3. **WHOIS correlation**
    4. **Registrar risk**
    5. **SSL risk**
    6. **Content risk**
    7. **DNS/MX (email capability)**
    8. **Suspicious hosting country**
    9. **Allowlist reductions** (official subdomains, approved registrars, approved email providers)
  - Calls `PhishingRiskScoringService.score(...)`
  - Updates the discovered domain with `riskScore`, `riskLevel`, `domainAgeDays`, `lastChecked`
  - If risk is HIGH, calls `ThreatService.persistIfHighRisk(...)`

### Signal Sources

#### 5.1 Similarity
- `SimilarityService`
  - Combines Jaro–Winkler + Levenshtein
  - Returns a percent score (0–100)

#### 5.2 Domain Age
- `DomainAgeCalculator`
  - Uses WHOIS creation date (if available)
  - Returns age in days

#### 5.3 WHOIS Correlation
- `WhoisAnalysisService`
  - Calls `DomainIqClient.getWhoisInfo(...)`
  - Stores/updates `WhoisFingerprintEntity`
  - Tracks `associatedDomainCount` per registrant email

#### 5.4 Registrar Risk
- `RegistrarRiskService`
  - Maps registrar name to risk level:
    - HIGH_ABUSE → 10
    - LOW_COST → 5
    - REPUTABLE → 0

#### 5.5 SSL Risk
- `SslAnalysisService`
  - Performs TLS handshake on port 443
  - Extracts issuer + validity
  - Flags:
    - Issued recently (< 3 days)
    - Short validity (< 30 days)
    - Unknown issuer

#### 5.6 Content Risk
- `ContentAnalysisService`
  - Fetches homepage (`https://domain`)
  - Detects:
    - Login form keywords
    - Brand keyword in body
    - Redirect chains

#### 5.7 DNS/MX Risk
- `DnsAnalysisService`
  - Uses DomainIQ DNS history
  - Detects MX presence (email capability)
  - Flags suspicious hosting countries based on A record country

## 6) Risk Scoring (Weights)

### Service
- `PhishingRiskScoringService`
  - Applies the expanded scoring rules:
    - Similarity: up to 40
    - Registered domain: 20
    - Domain age (<30d): 20
    - WHOIS correlation: up to 20
    - Registrar risk: up to 10
    - SSL risk: up to 10
    - SSL presence: 10
    - Content risk: up to 40
    - MX/email capability: 20
    - Suspicious hosting country: 10
    - Allowlist reductions: up to -20
  - Final score is capped to 0–100
  - Risk levels:
    - 0–20 → LOW
    - 21–50 → MEDIUM
    - 51–100 → HIGH

## 7) Threat Persistence (Final Output)

### Service
- `ThreatService`
  - Only persists if `riskLevel == HIGH`
  - Creates `ThreatEntity` with:
    - domainName
    - type = PHISHING
    - severity = HIGH
    - evidenceJson (WHOIS, registration date, similarity, SSL, content)
  - Stored in `ThreatRepository`

## 8) Fetching Results

### Controller
- `BrandProtectionController`
  - **Endpoint**: `GET /api/internal/brands/{id}/threats`
  - Returns a list of `ThreatDto` with summary info

### Service
- `ProtectedBrandService.getThreatsForBrand(...)`
  - Fetches discovered domains for brand
  - Collects threats by domain name
  - Returns `ThreatSummaryResponse`

## 9) Summary Flow (One-Liner)

User registers brand → Immediate processing triggers → DomainIQ discovery + active generation → DNS existence filter → Enrichment (WHOIS/SSL/content/DNS) → Risk scoring with allowlists → HIGH risk saved as threat → Threat summary exposed via API.

