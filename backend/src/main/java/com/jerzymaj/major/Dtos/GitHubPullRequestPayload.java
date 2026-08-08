package com.jerzymaj.major.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubPullRequestPayload(
        String action,
        @JsonProperty("pull_request") PullRequest pullRequest
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PullRequest(
            Head head,
            boolean merged
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Head(String ref) {
        }
    }
}
