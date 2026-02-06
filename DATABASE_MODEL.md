# Database Model and Persistence Flow

This document explains how the database tables relate to each other, what data each table stores, and which methods write to them.

## Tables and Relationships

### 1) `registered_domains`
**Purpose**: Stores each brand registered for monitoring.

**Key columns**
- `id` (UUID, PK)
- `user_id` (UUID)
- `brand_domain` (string)
- `brand_keyword` (string)
- `brand_name` (string)
- `status` (ACTIVE/PAUSED)
- `created_at` (timestamp)

**Written by**
- `ProtectedBrandService.registerBrand(...)`
  - Creates and saves a `ProtectedDomainEntity`.

**Relationship**
- One `registered_domains` record can have many `suspicious_domains_discovered` (1 → many).
  - It also owns multiple allowlist collections (see below).

---

### 2) `suspicious_domains_discovered`
**Purpose**: Stores suspicious domains discovered for a specific brand.

**Key columns**
- `id` (UUID, PK)
- `protected_domain_id` (UUID, FK → `registered_domains.id`)
- `domain_name` (string)
- `first_seen` (timestamp)
- `domain_age_days` (int)
- `registrar` (string)
- `country` (string)
- `similarity_score` (double)
- `risk_score` (int)
- `risk_level` (LOW/MEDIUM/HIGH)
- `last_checked` (timestamp)

**Written by**
- `DomainDiscoveryService.discover(...)`
  - Creates and saves new `DiscoveredDomainEntity` records for a brand.
- `DomainRiskAssessmentService.updateDiscoveredDomain(...)`
  - Updates `domain_age_days`, `risk_score`, `risk_level`, `last_checked`.

**Relationship**
- Many `suspicious_domains_discovered` belong to one `registered_domains` record.

---

### 3) `brand_keywords`
**Purpose**: Stores baseline keywords for the brand.

**Key columns**
- `brand_id` (UUID, FK → `registered_domains.id`)
- `keyword` (string)

**Written by**
- `ProtectedBrandService.registerBrand(...)`

---

### 4) `brand_official_subdomains`
**Purpose**: Stores official subdomains that should be treated as safe.

**Key columns**
- `brand_id` (UUID, FK → `registered_domains.id`)
- `subdomain` (string)

**Written by**
- `ProtectedBrandService.registerBrand(...)`

---

### 5) `brand_approved_registrars`
**Purpose**: Stores approved registrars for the brand.

**Key columns**
- `brand_id` (UUID, FK → `registered_domains.id`)
- `registrar` (string)

**Written by**
- `ProtectedBrandService.registerBrand(...)`

---

### 6) `brand_approved_email_providers`
**Purpose**: Stores approved email providers for the brand.

**Key columns**
- `brand_id` (UUID, FK → `registered_domains.id`)
- `email_provider` (string)

**Written by**
- `ProtectedBrandService.registerBrand(...)`

---

### 7) `whois_fingerprints`
**Purpose**: Tracks registrant fingerprints for correlation (campaign detection).

**Key columns**
- `id` (UUID, PK)
- `registrant_email` (string)
- `registrant_org` (string)
- `country` (string)
- `associated_domain_count` (int)
- `risk_score` (int)

**Written by**
- `WhoisAnalysisService.analyze(...)`
  - Finds by `registrant_email` and increments `associated_domain_count`.

**Relationship**
- No FK. Correlation is by `registrant_email`.

---

### 8) `threats`
**Purpose**: Stores HIGH risk threats with evidence JSON.

**Key columns**
- `id` (UUID, PK)
- `domain_name` (string)
- `type` (IMPERSONATION/PHISHING/SUSPICIOUS)
- `severity` (LOW/MEDIUM/HIGH)
- `evidence_json` (text)
- `detected_at` (timestamp)

**Written by**
- `ThreatService.persistIfHighRisk(...)`
  - Saves a `ThreatEntity` only when the risk level is HIGH.

**Relationship**
- No FK. Lookups are done by `domain_name` in `ProtectedBrandService.getThreatsForBrand(...)`.

---

## End-to-End Persistence Flow

1) **User registers brand**
   - `ProtectedBrandService.registerBrand(...)` → writes `registered_domains` and allowlist tables

2) **Immediate processing starts**
   - `MonitoringOrchestrator.runForBrand(...)`

3) **Discovery**
   - `DomainDiscoveryService.discover(...)` → writes `suspicious_domains_discovered`

4) **WHOIS correlation**
   - `WhoisAnalysisService.analyze(...)` → writes/updates `whois_fingerprints`

5) **Risk scoring**
   - `DomainRiskAssessmentService.updateDiscoveredDomain(...)` → updates `suspicious_domains_discovered`

6) **Threat creation**
   - `ThreatService.persistIfHighRisk(...)` → writes `threats` (only if HIGH)

---

## Quick Summary
- `registered_domains` is the parent.
- `suspicious_domains_discovered` is the per-brand child.
- `whois_fingerprints` is global registrant correlation.
- `threats` stores only high-risk detections with evidence JSON.
