package com.example.BrandProtection.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimilarDomainRepository extends JpaRepository<SimilarDomainEntity, UUID> {
    void deleteByProtectedDomainId(UUID protectedDomainId);
}
