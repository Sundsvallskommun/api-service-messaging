package se.sundsvall.messaging.integration.emailsender;

import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record EmailDto(
	Sender sender,
	String emailAddress,
	String subject,
	String message,
	String htmlMessage,
	List<Attachment> attachments,
	Map<String, List<String>> headers) {

	@Builder(setterPrefix = "with")
	public record Sender(
		String name,
		String address,
		String replyTo) {}

	@Builder(setterPrefix = "with")
	public record Attachment(
		String name,
		String contentType,
		String content) {}
}
