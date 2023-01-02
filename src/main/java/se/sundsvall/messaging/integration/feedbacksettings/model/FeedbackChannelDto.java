package se.sundsvall.messaging.integration.feedbacksettings.model;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record FeedbackChannelDto(
    ContactMethod contactMethod,
    String destination,
    boolean feedbackWanted) {  }
