package com.example.BrandProtection.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhoisFingerprintRepository extends JpaRepository<WhoisFingerprintEntity, UUID> {
    Optional<WhoisFingerprintEntity> findByRegistrantEmail(String registrantEmail);
}
