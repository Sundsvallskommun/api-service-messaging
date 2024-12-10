package se.sundsvall.messaging.integration.emailsender;

import generated.se.sundsvall.emailsender.Attachment;
import generated.se.sundsvall.emailsender.SendEmailRequest;
import generated.se.sundsvall.emailsender.Sender;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class EmailSenderIntegrationMapper {

	SendEmailRequest toSendEmailRequest(final EmailDto dto) {
		if (dto == null) {
			return null;
		}

		return new SendEmailRequest()
			.sender(new Sender()
				.name(dto.sender().name())
				.address(dto.sender().address())
				.replyTo(dto.sender().replyTo()))
			.emailAddress(dto.emailAddress())
			.subject(dto.subject())
			.message(dto.message())
			.htmlMessage(dto.htmlMessage())
			.headers(dto.headers())
			.attachments(Optional.ofNullable(dto.attachments())
				.map(attachments -> attachments.stream()
					.map(attachment -> new Attachment()
						.name(attachment.name())
						.contentType(attachment.contentType())
						.content(attachment.content()))
					.toList())
				.orElse(null));

	}
}
