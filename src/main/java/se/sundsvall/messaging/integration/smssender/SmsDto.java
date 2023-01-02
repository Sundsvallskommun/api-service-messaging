package se.sundsvall.messaging.integration.smssender;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record SmsDto(
    String sender,
    String mobileNumber,
    String message) {  }
