package com.example.BrandProtection.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscoveredDomainRepository extends JpaRepository<DiscoveredDomainEntity, UUID> {
    List<DiscoveredDomainEntity> findByProtectedDomainId(UUID protectedDomainId);
}
