package com.example.afisha.dto;

import java.util.List;

public class KudaGoResponceDto {

    private List<KudaGoEventDto> results;

    public KudaGoResponceDto() {

    }

    public List<KudaGoEventDto> getResults() {
        return results;
    }

    public void setResults(List<KudaGoEventDto> results) {
        this.results = results;
    }
}
