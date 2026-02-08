package com.example.BrandProtection;

import com.example.BrandProtection.domainiq.DomainIqProperties;
import com.example.BrandProtection.service.DomainDiscoveryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({DomainIqProperties.class, DomainDiscoveryProperties.class})
@EnableCaching
@EnableScheduling
@EnableAsync
public class DomainBrandProtectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomainBrandProtectionApplication.class, args);
    }
}
