package se.sundsvall.messaging.integration.slack;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record SlackDto(String token, String channel, String message) {  }
