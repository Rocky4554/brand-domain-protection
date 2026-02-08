package com.example.BrandProtection.domainiq.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainSearchResponse {
    private String query;
    @JsonProperty("total_results")
    private Integer totalResults;
    @JsonAlias({"items", "results"})
    private List<DomainSearchItem> domains;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public List<DomainSearchItem> getDomains() {
        return domains;
    }

    public void setDomains(List<DomainSearchItem> domains) {
        this.domains = domains;
    }
}
