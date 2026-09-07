package se.sundsvall.messaging.integration.rabbitmq;

import java.util.List;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.Priority;
import se.sundsvall.messaging.api.model.request.SmsRequest;

import static java.util.Optional.ofNullable;

final class RabbitMapper {

	private RabbitMapper() {}

	/**
	 * Reduces an identifier to the value that {@code issuer} holds.
	 * <p>
	 * Over HTTP the value arrives as an {@code X-Sent-By} header and dept44's filter unpacks it, so
	 * {@code MessageResource} only ever sees {@code Identifier.getValue()}. Nothing unpacks it on this path, so without
	 * this the whole {@code "mar14han; type=adAccount"} would be stored where every other row holds {@code "mar14han"},
	 * splitting the column that user history is filtered by.
	 * <p>
	 * The type is deliberately dropped rather than stored: the value alone is what {@code issuer} has always held, and
	 * it can be an AD account or a party id either way.
	 *
	 * @param sentBy an identifier in header syntax, or a bare value
	 */
	static String toIssuer(final String sentBy) {
		// parse() returns null for anything that is not header syntax, a bare value included.
		return ofNullable(Identifier.parse(sentBy))
			.map(Identifier::getValue)
			.orElse(sentBy);
	}

	/**
	 * A queued request is turned into the same request object the REST endpoint builds, so it travels the ordinary
	 * delivery path and earns the same history and statistics entries. {@code sentBy} lands on {@code issuer} and
	 * {@code origin} on {@code origin}, which is where those two values sit for a request that arrived over HTTP.
	 */
	static SmsRequest toSmsRequest(final SmsQueueMessage message) {
		if (message == null) {
			return null;
		}

		return SmsRequest.builder()
			.withParty(SmsRequest.Party.builder()
				.withPartyId(message.partyId())
				.build())
			.withSender(message.sender())
			.withMobileNumber(message.mobileNumber())
			.withMessage(message.message())
			.withDepartment(message.department())
			.withMunicipalityId(message.municipalityId())
			.withOrigin(message.origin())
			.withIssuer(toIssuer(message.sentBy()))
			.withPriority(Priority.NORMAL)
			.build();
	}

	/**
	 * The e-mail counterpart of {@link #toSmsRequest}. Attachments keep their references rather than being fetched here:
	 * resolution belongs in the delivery attempt, where a failure to read an object is classified alongside a failure to
	 * reach email-sender instead of escaping past the ladder.
	 */
	static EmailRequest toEmailRequest(final EmailQueueMessage message) {
		if (message == null) {
			return null;
		}

		return EmailRequest.builder()
			.withParty(EmailRequest.Party.builder()
				.withPartyId(message.partyId())
				.build())
			.withEmailAddress(message.emailAddress())
			.withSubject(message.subject())
			.withMessage(message.message())
			.withHtmlMessage(message.htmlMessage())
			.withSender(EmailRequest.Sender.builder()
				.withName(message.senderName())
				.withAddress(message.senderAddress())
				.withReplyTo(message.replyTo())
				.build())
			.withAttachments(toAttachments(message.attachments()))
			.withMunicipalityId(message.municipalityId())
			.withOrigin(message.origin())
			.withIssuer(toIssuer(message.sentBy()))
			.build();
	}

	private static List<EmailRequest.Attachment> toAttachments(final List<EmailQueueMessage.Attachment> attachments) {
		return ofNullable(attachments).orElse(List.of()).stream()
			.map(attachment -> EmailRequest.Attachment.builder()
				.withName(attachment.name())
				.withContentType(attachment.contentType())
				.withObjectId(attachment.objectId())
				.build())
			.toList();
	}
}
