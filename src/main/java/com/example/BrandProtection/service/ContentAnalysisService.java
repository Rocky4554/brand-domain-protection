package com.example.BrandProtection.service;

import java.time.Duration;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

@Service
public class ContentAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(ContentAnalysisService.class);

    private final WebClient webClient;

    public ContentAnalysisService() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(5));
        this.webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    @Cacheable(cacheNames = "contentScan", key = "#domain + ':' + #brandKeyword")
    public ContentRiskFlags scan(String domain, String brandKeyword) {
        ContentRiskFlags flags = new ContentRiskFlags();
        if (domain == null || domain.isBlank()) {
            return flags;
        }
        try {
            String url = "https://" + domain;
            String body = webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is3xxRedirection, response -> {
                    flags.setRedirectChain(true);
                    return response.createException();
                })
                .bodyToMono(String.class)
                .block();

            if (body == null) {
                return flags;
            }

            String content = body.toLowerCase(Locale.ROOT);
            flags.setLoginFormDetected(content.contains("type=\"password\"") || content.contains("login"));
            if (brandKeyword != null && !brandKeyword.isBlank()) {
                flags.setBrandKeywordsPresent(content.contains(brandKeyword.toLowerCase(Locale.ROOT)));
            }
        } catch (Exception ex) {
            logger.info("Content scan failed for {}: {}", domain, ex.getMessage());
        }
        return flags;
    }
}
