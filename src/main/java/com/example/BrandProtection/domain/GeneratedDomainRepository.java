package com.example.BrandProtection.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedDomainRepository extends JpaRepository<GeneratedDomainEntity, UUID> {
    Optional<GeneratedDomainEntity> findByProtectedDomainIdAndDomainName(UUID protectedDomainId, String domainName);
    void deleteByProtectedDomainId(UUID protectedDomainId);
}
