package com.example.BrandProtection.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProtectedDomainRepository extends JpaRepository<ProtectedDomainEntity, UUID> {
    List<ProtectedDomainEntity> findByStatus(BrandStatus status);
}
