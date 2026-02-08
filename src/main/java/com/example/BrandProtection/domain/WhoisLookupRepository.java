package com.example.BrandProtection.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhoisLookupRepository extends JpaRepository<WhoisLookupEntity, UUID> {
    Optional<WhoisLookupEntity> findByBrandIdAndDomainName(UUID brandId, String domainName);
    void deleteByBrandId(UUID brandId);
}
