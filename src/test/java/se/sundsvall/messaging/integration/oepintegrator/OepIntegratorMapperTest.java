package se.sundsvall.messaging.integration.oepintegrator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.sundsvall.messaging.model.ExternalReference;

class OepIntegratorMapperTest {

	private final OepIntegratorMapper mapper = new OepIntegratorMapper();

	@ParameterizedTest
	@MethodSource("argumentProvider")
	void toWebmessageRequest(final String userId, final String partyId, final boolean sendAsOwner) {
		var externalReference = new ExternalReference("key", "value");
		var externalReferences = List.of(externalReference);
		var message = "message";
		var oepInstance = "oepInstance";
		var attachments = List.of(new WebMessageDto.Attachment("fileName", "application/pdf", "content"));
		var webMessageDto = new WebMessageDto(partyId, externalReferences, message, userId, oepInstance, sendAsOwner, attachments);

		var result = mapper.toWebmessageRequest(webMessageDto);

		if (sendAsOwner) {
			assertThat(result.getSender().getAdministratorId()).isEqualTo(userId);
			assertThat(result.getSender().getPartyId()).isNull();
			assertThat(result.getSender().getUserId()).isNull();
		} else {
			assertThat(result.getSender().getAdministratorId()).isNull();
			assertThat(result.getSender().getPartyId()).isEqualTo(partyId);
			assertThat(result.getSender().getUserId()).isEqualTo(userId);
		}
		assertThat(result.getMessage()).isEqualTo(message);
		assertThat(result.getExternalReferences()).allSatisfy(reference -> assertThat(reference).usingRecursiveComparison().isEqualTo(mapper.toExternalReference(externalReference)));
	}

	private static Stream<Arguments> argumentProvider() {
		return Stream.of(
			Arguments.of("userId", "partyId", false),
			Arguments.of("userId", "partyId", true));
	}

	@Test
	void toExternalReferences() {
		var externalReference = new ExternalReference("key", "value");
		var externalReferences = List.of(externalReference, externalReference);

		var result = mapper.toExternalReferences(externalReferences);

		assertThat(result).hasSize(2);
		assertThat(result).allSatisfy(reference -> assertThat(reference).usingRecursiveComparison().isEqualTo(mapper.toExternalReference(externalReference)));
	}

	@Test
	void toExternalReference() {
		var externalReference = new ExternalReference("key", "value");

		var result = mapper.toExternalReference(externalReference);

		assertThat(result.getKey()).isEqualTo("key");
		assertThat(result.getValue()).isEqualTo("value");
	}

	@Test
	void toInputStream() {
		var base64Data = Base64.getEncoder().encodeToString("Test".getBytes());

		var inputStream = mapper.toInputStream(base64Data);

		assertThat(inputStream).isNotNull();
	}

}
