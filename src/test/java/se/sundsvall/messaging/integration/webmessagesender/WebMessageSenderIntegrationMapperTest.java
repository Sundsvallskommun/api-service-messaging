package se.sundsvall.messaging.integration.webmessagesender;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createExternalReference;

import generated.se.sundsvall.webmessagesender.Attachment;
import generated.se.sundsvall.webmessagesender.CreateWebMessageRequest;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class WebMessageSenderIntegrationMapperTest {

	private final WebMessageSenderIntegrationMapper mapper = new WebMessageSenderIntegrationMapper();

	@Test
	void test_toCreateWebMessageRequest_whenDtoIsNull() {
		assertThat(mapper.toCreateWebMessageRequest(null)).isNull();
	}

	@Test
	void test_toCreateWebMessageRequest() {
		final var dto = WebMessageDto.builder()
			.withPartyId("somePartyId")
			.withExternalReferences(List.of(createExternalReference()))
			.withMessage("someMessage")
			.withOepInstance("internal")
			.withAttachments(List.of(WebMessageDto.Attachment.builder()
				.withBase64Data("someData")
				.withFileName("someFilename")
				.withMimeType("someMimeType")
				.build()))
			.build();

		final var mappedRequest = mapper.toCreateWebMessageRequest(dto);

		assertThat(mappedRequest.getPartyId()).isEqualTo(dto.partyId());
		assertThat(mappedRequest.getOepInstance()).isEqualTo(CreateWebMessageRequest.OepInstanceEnum.fromValue(dto.oepInstance()));
		assertThat(mappedRequest.getExternalReferences()).hasSameSizeAs(dto.externalReferences());
		assertThat(mappedRequest.getMessage()).isEqualTo(dto.message());
		assertThat(mappedRequest.getAttachments()).hasSize(1).extracting(Attachment::getBase64Data, Attachment::getFileName, Attachment::getMimeType)
			.containsExactly(Tuple.tuple("someData", "someFilename", "someMimeType"));
	}
}
