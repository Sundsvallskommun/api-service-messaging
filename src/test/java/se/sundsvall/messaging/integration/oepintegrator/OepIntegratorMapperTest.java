package se.sundsvall.messaging.integration.oepintegrator;

import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.sundsvall.messaging.model.ExternalReference;

import static org.assertj.core.api.Assertions.assertThat;

class OepIntegratorMapperTest {

	private final OepIntegratorMapper mapper = new OepIntegratorMapper();

	private static Stream<Arguments> argumentProvider() {
		return Stream.of(
			Arguments.of("userId", "partyId", false),
			Arguments.of("userId", "partyId", true));
	}

	@ParameterizedTest
	@MethodSource("argumentProvider")
	void toWebmessageRequest(final String userId, final String partyId, final boolean sendAsOwner) {
		final var externalReference = new ExternalReference("key", "value");
		final var externalReferences = List.of(externalReference);
		final var message = "message";
		final var oepInstance = "oepInstance";
		final var attachments = List.of(new WebMessageDto.Attachment("fileName", "application/pdf", "content"));
		final var webMessageDto = new WebMessageDto(partyId, externalReferences, message, userId, oepInstance, sendAsOwner, attachments);

		final var result = mapper.toWebmessageRequest(webMessageDto);

		if (sendAsOwner) {
			assertThat(result.getSender().getAdministratorId()).isNull();
			assertThat(result.getSender().getPartyId()).isEqualTo(partyId);
			assertThat(result.getSender().getUserId()).isEqualTo(userId);
		} else {
			assertThat(result.getSender().getAdministratorId()).isEqualTo(userId);
			assertThat(result.getSender().getPartyId()).isNull();
			assertThat(result.getSender().getUserId()).isNull();
		}
		assertThat(result.getMessage()).isEqualTo(message);
		assertThat(result.getExternalReferences()).allSatisfy(reference -> assertThat(reference).usingRecursiveComparison().isEqualTo(mapper.toExternalReference(externalReference)));
	}

	@Test
	void toExternalReferences() {
		final var externalReference = new ExternalReference("key", "value");
		final var externalReferences = List.of(externalReference, externalReference);

		final var result = mapper.toExternalReferences(externalReferences);

		assertThat(result).hasSize(2)
			.allSatisfy(reference -> assertThat(reference).usingRecursiveComparison().isEqualTo(mapper.toExternalReference(externalReference)));
	}

	@Test
	void toExternalReference() {
		final var externalReference = new ExternalReference("key", "value");

		final var result = mapper.toExternalReference(externalReference);

		assertThat(result.getKey()).isEqualTo("key");
		assertThat(result.getValue()).isEqualTo("value");
	}

	@Test
	void toInputStream() {
		final var base64Data = Base64.getEncoder().encodeToString("Test".getBytes());

		final var inputStream = mapper.toInputStream(base64Data);

		assertThat(inputStream).isNotNull();
	}

}
