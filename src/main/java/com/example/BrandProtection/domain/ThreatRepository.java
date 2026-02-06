package com.example.BrandProtection.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreatRepository extends JpaRepository<ThreatEntity, UUID> {
    List<ThreatEntity> findByDomainName(String domainName);
}
