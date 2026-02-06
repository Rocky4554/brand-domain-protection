package com.example.BrandProtection;

import com.example.BrandProtection.service.DomainVariationGenerator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainVariationGeneratorTests {

    @Test
    void generatesExpectedVariants() {
        DomainVariationGenerator generator = new DomainVariationGenerator();
        Set<String> variations = generator.generate("flipkart.com", List.of("flipkart"));

        assertTrue(variations.contains("f1ipkart"));
        assertTrue(variations.contains("flipkart.com"));
        assertTrue(variations.contains("flipkart.in"));
    }
}
