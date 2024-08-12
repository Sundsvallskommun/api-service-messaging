package se.sundsvall.messaging.integration.snailmailsender;

import java.util.Optional;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.snailmail.Attachment;
import generated.se.sundsvall.snailmail.SendSnailMailRequest;

@Component
class SnailMailSenderIntegrationMapper {

	SendSnailMailRequest toSendSnailmailRequest(final SnailMailDto dto) {
		if (dto == null) {
			return null;
		}

		return new SendSnailMailRequest()
			.partyId(dto.partyId())
			.batchId(dto.batchId())
			.department(dto.department())
			.deviation(dto.deviation())
			.attachments(Optional.ofNullable(dto.attachments())
				.map(attachments -> attachments.stream()
					.map(attachment -> new Attachment()
						.name(attachment.name())
						.contentType(Attachment.ContentTypeEnum.fromValue(attachment.contentType()))
						.content(attachment.content()))
					.toList()
				)
				.orElse(null));
	}

}
