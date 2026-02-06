# Postman Requests and Sample Responses

This document contains ready-to-use Postman requests with example responses.

## 1) Register Brand

**Method**: `POST`  
**URL**: `http://localhost:8080/api/internal/brands`  
**Headers**:  
`Content-Type: application/json`

**Body (raw JSON)**:
```json
{
  "userId": "11111111-1111-1111-1111-111111111111",
  "brandName": "Flipkart",
  "brandDomain": "flipkart.com",
  "brandKeyword": "flipkart",
  "keywords": ["flipkart", "flip", "kart"],
  "officialSubdomains": ["pay.flipkart.com", "seller.flipkart.com"],
  "approvedRegistrars": ["GoDaddy", "NameCheap"],
  "approvedEmailProviders": ["google.com", "outlook.com"]
}
```

**Sample Response (201)**:
```json
{
  "id": "7b6e6f2c-4d68-4b8f-9c0d-6c3c8a2b2c11"
}
```

---

## 2) Get Threats for Brand

**Method**: `GET`  
**URL**: `http://localhost:8080/api/internal/brands/{{brandId}}/threats`  
**Headers**:  
`Content-Type: application/json`

**Sample Response (200)**:
```json
{
  "totalThreats": 1,
  "threats": [
    {
      "id": "b1ab7e7c-1b25-4c2b-9b3a-8f7fb0e2b5aa",
      "domainName": "fl1pkart-support.com",
      "type": "PHISHING",
      "severity": "HIGH",
      "detectedAt": "2026-02-06T12:30:10Z"
    }
  ]
}
```

---

## 3) Health Check (optional)

**Method**: `GET`  
**URL**: `http://localhost:8080/actuator/health`

**Sample Response (200)**:
```json
{
  "status": "UP"
}
```
