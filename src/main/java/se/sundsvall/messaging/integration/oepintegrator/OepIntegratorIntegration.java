package se.sundsvall.messaging.integration.oepintegrator;

import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.api.model.request.WebMessageRequest.Attachment;
import se.sundsvall.messaging.model.MessageStatus;

@Component
public class OepIntegratorIntegration {

	static final String INTEGRATION_NAME = "OepIntegrator";

	private final OepIntegratorClient client;
	private final OepIntegratorMapper mapper;

	OepIntegratorIntegration(final OepIntegratorClient client, final OepIntegratorMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public MessageStatus sendWebMessage(final String municipalityId, final WebMessageDto webMessageDto, final List<Attachment> attachments) {
		final var request = mapper.toWebmessageRequest(webMessageDto);

		var multipartFiles = Optional.ofNullable(attachments)
			.map(mapper::toAttachmentMultipartFiles)
			.orElse(null);

		final var response = client.createWebmessage(municipalityId, webMessageDto.oepInstance(), request, multipartFiles);

		return response.getStatusCode().is2xxSuccessful() ? SENT : NOT_SENT;
	}

}
