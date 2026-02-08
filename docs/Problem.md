# Problem Statement and Root Cause (Interview Notes)

## Problem Summary
During testing, **domain discovery failed** immediately after brand registration. The discovery pipeline threw a `DomainIqException` and no domains were discovered or scored.

## Symptoms
- Error logged during discovery:
  - `DomainIQ search failed: ... 404 NOT_FOUND`
- HTML response from DomainIQ instead of JSON
- No entries created in `suspicious_domains_discovered`

## Root Cause
The **DomainIQ endpoint format was incorrect** in the client implementation.

**What the client expected**
```
GET https://www.domainiq.com/api/v1/domains/search?query=flipkart*
```

**What DomainIQ actually requires**
```
GET https://www.domainiq.com/api?key=...&service=domain_search&keyword=flipkart&condition=contains&output_mode=json
```

The wrong path (`/api/v1/...`) caused a **404 Page Not Found** and returned HTML, which the client treated as an error.

## Fix Applied
- Updated `DomainIqClient` to use **query parameters**: `key` and `service`.
- Removed the hardcoded `/v1/...` paths.
- Added configurable service names in `application-*.properties`.

**New format**
```
GET https://www.domainiq.com/api?key=<API_KEY>&service=domain_search&keyword=flipkart&condition=contains&output_mode=json
GET https://www.domainiq.com/api?key=<API_KEY>&service=whois&domain=flipkart.com&output_mode=json
GET https://www.domainiq.com/api?key=<API_KEY>&service=dns&q=flipkart.com&output_mode=json
```

## Result
- Domain discovery now calls the correct API.
- 404 errors are eliminated.
- Discovery can proceed to DNS filtering and risk scoring.

## Lessons Learned
- Validate real API contract early using curl or Postman.
- Handle HTML responses gracefully (log raw response + status).
- Keep third‑party service names configurable to avoid hardcoding.

---

## Additional Issue: LazyInitializationException in @Async
When running discovery asynchronously, the async thread accessed lazy JPA collections
(`keywords`, `officialSubdomains`, `approvedRegistrars`, `approvedEmailProviders`) and
threw `LazyInitializationException`.

### Root Cause
JPA lazy collections are only initialized inside an active Hibernate session. The
`@Async` method runs on a different thread, after the session is closed.

### Fix Applied (Industry Standard)
- Fetch required data in the request thread
- Convert to a DTO snapshot
- Pass the DTO into `@Async`

This avoids lazy-loading errors, prevents extra DB hits in async, and makes
performance predictable and thread-safe.

### Implementation Detail
The snapshot is built **before** entering the async method, inside a
transactional method, then passed into `runForBrandAsync(...)`.

---

## Additional Issue: Invalid dates from DomainIQ
During discovery, DomainIQ sometimes returns `registration_date` as `0000-00-00`.
This is not a valid ISO date and caused `DateTimeParseException` in
`DomainDiscoveryService.parseDate(...)`.

### Fix Applied
- Treat `0000-00-00` as unknown date
- Guard parsing with a try/catch and log a warning

---

## Additional Issue: DomainIQ timeouts during DNS checks
Discovery sometimes timed out while calling DomainIQ DNS history. This raised
`ReadTimeoutException` and stopped async discovery.

### Fix Applied
- Increased `domainiq.timeout-ms`
- Added retry with backoff for timeout/network errors (not for 4xx)
- Limited per‑brand candidate counts to reduce total outbound calls
- Added concurrency limiter (semaphore) to prevent burst traffic
- Added a simple circuit breaker to stop hammering DomainIQ when failing

### Config keys involved
- `domainiq.timeout-ms`
- `domainiq.retry-max-attempts`
- `domainiq.retry-backoff-ms`
- `domainiq.max-concurrent-requests`
- `domainiq.circuit-breaker-enabled`
- `domainiq.circuit-breaker-failure-threshold`
- `domainiq.circuit-breaker-open-ms`
- `discovery.max-domainiq-results`
- `discovery.max-generated`
- `discovery.max-candidates`
