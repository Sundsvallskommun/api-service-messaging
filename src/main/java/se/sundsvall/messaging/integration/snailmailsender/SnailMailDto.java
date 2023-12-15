package se.sundsvall.messaging.integration.snailmailsender;

import java.util.List;

import se.sundsvall.messaging.api.model.request.EnvelopeType;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record SnailMailDto(
        String department,
        String deviation,
        List<Attachment> attachments) {

    @Builder(setterPrefix = "with")
    public record Attachment(
            String name,
            String contentType,
            String content,
            EnvelopeType envelopeType) { }
}
