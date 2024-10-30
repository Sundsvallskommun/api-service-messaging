package se.sundsvall.messaging.integration.digitalmailsender;

import java.util.List;

import se.sundsvall.messaging.model.ContentType;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record DigitalMailDto(
	Sender sender,
	String partyId,
	String subject,
	ContentType contentType,
	String body,
	List<Attachment> attachments) {

	@Builder(setterPrefix = "with")
	public record Sender(
		String municipalityId,
		SupportInfo supportInfo) {

		@Builder(setterPrefix = "with")
		public record SupportInfo(
			String text,
			String emailAddress,
			String phoneNumber,
			String url) {}
	}

	@Builder(setterPrefix = "with")
	public record Attachment(
		String filename,
		ContentType contentType,
		String content) {}
}
