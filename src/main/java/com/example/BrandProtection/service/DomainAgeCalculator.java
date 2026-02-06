package com.example.BrandProtection.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class DomainAgeCalculator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    public int domainAgeInDays(String creationDate) {
        if (creationDate == null || creationDate.isBlank()) {
            return -1;
        }
        LocalDate created = LocalDate.parse(creationDate, DATE_FORMATTER);
        return (int) ChronoUnit.DAYS.between(created, LocalDate.now(ZoneOffset.UTC));
    }

    public int domainAgeInDays(java.time.Instant firstSeen) {
        if (firstSeen == null) {
            return -1;
        }
        LocalDate created = firstSeen.atZone(ZoneId.of("UTC")).toLocalDate();
        return (int) ChronoUnit.DAYS.between(created, LocalDate.now(ZoneOffset.UTC));
    }

    public boolean isNewDomain(int domainAgeDays, int thresholdDays) {
        return domainAgeDays >= 0 && domainAgeDays < thresholdDays;
    }
}
