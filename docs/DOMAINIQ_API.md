# DomainIQ API Calls (Current)

This document lists the exact DomainIQ endpoints used by the app and where they are called.

## 1) Candidate domain search
**Used by**: `DomainIqClient.searchDomainsByKeyword(...)`  
**Purpose**: Fetch candidate domains matching the brand keyword.

**Endpoint**
```
GET https://www.domainiq.com/api
  ?key=<API_KEY>
  &service=domain_search
  &keyword=<brandKeyword*>
  &condition=contains
  &output_mode=json
```

## 2) WHOIS lookup
**Used by**: `DomainIqClient.getWhoisInfo(...)`  
**Purpose**: Fetch WHOIS details for a suspicious domain.

**Endpoint**
```
GET https://www.domainiq.com/api
  ?key=<API_KEY>
  &service=whois
  &domain=<domain>
  &output_mode=json
```

## 3) DNS history / DNS records
**Used by**: `DomainIqClient.getDnsHistory(...)`  
**Purpose**: Check if generated domains are registered (A/MX/NS).

**Endpoint**
```
GET https://www.domainiq.com/api
  ?key=<API_KEY>
  &service=dns
  &q=<domain>
  &output_mode=json
```

---

## Config keys
These are set in `application-dev.properties` / `application-prod.properties`:
- `domainiq.base-url=https://www.domainiq.com/api`
- `domainiq.api-key=<API_KEY>`
- `domainiq.search-service=domain_search`
- `domainiq.search-condition=contains`
- `domainiq.whois-service=whois`
- `domainiq.dns-service=dns`
- `domainiq.output-mode=json`

## Notes
- The client uses **query parameters**, not `/v1/...` paths.
- Timeouts, retries, and concurrency limits are configurable in properties.
