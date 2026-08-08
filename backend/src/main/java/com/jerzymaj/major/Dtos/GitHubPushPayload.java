package com.jerzymaj.major.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubPushPayload(
        String ref,
        List<Commit> commits
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Commit(String message) {
    }
}
