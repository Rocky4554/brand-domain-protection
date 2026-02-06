package com.example.BrandProtection.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DomainVariationGenerator {
    private static final List<String> COMMON_TLDS = List.of("com", "net", "org", "in", "co");

    public Set<String> generate(String primaryDomain, List<String> keywords) {
        Set<String> variations = new HashSet<>();
        String base = stripTld(primaryDomain);

        if (base != null) {
            variations.addAll(generateForToken(base));
            variations.addAll(applyTlds(base));
        }

        if (keywords != null) {
            for (String keyword : keywords) {
                if (keyword == null || keyword.isBlank()) {
                    continue;
                }
                String normalized = keyword.toLowerCase();
                variations.addAll(generateForToken(normalized));
                variations.addAll(applyTlds(normalized));
            }
        }

        return variations;
    }

    private Set<String> generateForToken(String token) {
        Set<String> variations = new HashSet<>();
        variations.addAll(missingCharacter(token));
        variations.addAll(doubleCharacter(token));
        variations.addAll(swapAdjacent(token));
        variations.addAll(homoglyphs(token));
        return variations;
    }

    private Set<String> missingCharacter(String token) {
        Set<String> results = new HashSet<>();
        for (int i = 0; i < token.length(); i++) {
            results.add(token.substring(0, i) + token.substring(i + 1));
        }
        return results;
    }

    private Set<String> doubleCharacter(String token) {
        Set<String> results = new HashSet<>();
        for (int i = 0; i < token.length(); i++) {
            results.add(token.substring(0, i) + token.charAt(i) + token.charAt(i) + token.substring(i + 1));
        }
        return results;
    }

    private Set<String> swapAdjacent(String token) {
        Set<String> results = new HashSet<>();
        for (int i = 0; i < token.length() - 1; i++) {
            char[] chars = token.toCharArray();
            char temp = chars[i];
            chars[i] = chars[i + 1];
            chars[i + 1] = temp;
            results.add(new String(chars));
        }
        return results;
    }

    private Set<String> homoglyphs(String token) {
        Set<String> results = new HashSet<>();
        results.add(token.replace("l", "1"));
        results.add(token.replace("o", "0"));
        results.add(token.replace("i", "1"));
        results.add(token.replace("a", "@"));
        return results;
    }

    private Set<String> applyTlds(String token) {
        Set<String> results = new HashSet<>();
        for (String tld : COMMON_TLDS) {
            results.add(token + "." + tld);
        }
        return results;
    }

    private String stripTld(String domain) {
        if (domain == null || !domain.contains(".")) {
            return domain;
        }
        return domain.substring(0, domain.lastIndexOf('.')).toLowerCase();
    }
}
