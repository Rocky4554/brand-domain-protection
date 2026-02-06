package com.example.BrandProtection.service;

import org.springframework.stereotype.Service;

@Service
public class SimilarityService {

    public int similarityPercent(String a, String b) {
        if (a == null || b == null) {
            return 0;
        }
        String left = normalize(a);
        String right = normalize(b);
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        double jw = jaroWinkler(left, right);
        double lev = 1.0 - ((double) levenshteinDistance(left, right) / Math.max(left.length(), right.length()));
        double score = Math.max(jw, lev);
        return (int) Math.round(score * 100.0);
    }

    private String normalize(String value) {
        return value.toLowerCase()
            .replace("http://", "")
            .replace("https://", "")
            .replace("www.", "")
            .trim();
    }

    private int levenshteinDistance(String left, String right) {
        int[] costs = new int[right.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            costs[0] = i;
            int northwest = i - 1;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                int north = costs[j] + 1;
                int west = costs[j - 1] + 1;
                int current = Math.min(Math.min(north, west), northwest + cost);
                northwest = costs[j];
                costs[j] = current;
            }
        }
        return costs[right.length()];
    }

    private double jaroWinkler(String left, String right) {
        int leftLen = left.length();
        int rightLen = right.length();
        if (leftLen == 0 && rightLen == 0) {
            return 1.0;
        }
        int matchDistance = Math.max(leftLen, rightLen) / 2 - 1;
        boolean[] leftMatches = new boolean[leftLen];
        boolean[] rightMatches = new boolean[rightLen];
        int matches = 0;
        for (int i = 0; i < leftLen; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, rightLen);
            for (int j = start; j < end; j++) {
                if (rightMatches[j]) {
                    continue;
                }
                if (left.charAt(i) != right.charAt(j)) {
                    continue;
                }
                leftMatches[i] = true;
                rightMatches[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) {
            return 0.0;
        }
        double transpositions = 0;
        int k = 0;
        for (int i = 0; i < leftLen; i++) {
            if (!leftMatches[i]) {
                continue;
            }
            while (!rightMatches[k]) {
                k++;
            }
            if (left.charAt(i) != right.charAt(k)) {
                transpositions++;
            }
            k++;
        }
        transpositions /= 2.0;
        double jaro = ((matches / (double) leftLen)
            + (matches / (double) rightLen)
            + ((matches - transpositions) / matches)) / 3.0;
        int prefix = 0;
        for (int i = 0; i < Math.min(4, Math.min(leftLen, rightLen)); i++) {
            if (left.charAt(i) == right.charAt(i)) {
                prefix++;
            } else {
                break;
            }
        }
        return jaro + prefix * 0.1 * (1 - jaro);
    }
}
