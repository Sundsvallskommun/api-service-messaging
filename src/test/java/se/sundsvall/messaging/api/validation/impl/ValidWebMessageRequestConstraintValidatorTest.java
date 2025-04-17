package se.sundsvall.messaging.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;

@ExtendWith(MockitoExtension.class)
class ValidWebMessageRequestConstraintValidatorTest {

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private ConstraintValidatorContext mockContext;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private ConstraintValidatorContext.ConstraintViolationBuilder mockConstraintViolationBuilder;

	@InjectMocks
	private final ValidWebMessageRequestConstraintValidator validator = new ValidWebMessageRequestConstraintValidator();

	@ParameterizedTest
	@MethodSource("invalidSendAsOwnerArgumentProvider")
	void invalidSendAsOwnerRequest(Boolean sendAsOwner, String userId, String partyId) {
		when(mockContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(mockConstraintViolationBuilder);

		var request = WebMessageRequest.builder()
			.withSendAsOwner(sendAsOwner)
			.withSender(WebMessageRequest.Sender.builder().withUserId(userId).build())
			.withParty(WebMessageRequest.Party.builder().withPartyId(partyId).build())
			.build();

		assertThat(validator.isValid(request, mockContext)).isFalse();
	}

	@ParameterizedTest
	@MethodSource("validSendAsOwnerArgumentProvider")
	void validSendAsOwnerRequest(Boolean sendAsOwner, String userId, String partyId) {
		var request = WebMessageRequest.builder()
			.withSendAsOwner(sendAsOwner)
			.withSender(WebMessageRequest.Sender.builder().withUserId(userId).build())
			.withParty(WebMessageRequest.Party.builder().withPartyId(partyId).build())
			.build();

		assertThat(validator.isValid(request, mockContext)).isTrue();
	}

	@Test
	void validSendAsAdministratorRequest() {
		var request = WebMessageRequest.builder()
			.withSendAsOwner(false)
			.withSender(WebMessageRequest.Sender.builder().withUserId("userId").build())
			.withParty(WebMessageRequest.Party.builder().withPartyId("partyId").build())
			.build();

		assertThat(validator.isValid(request, mockContext)).isTrue();
	}

	private static Stream<Arguments> invalidSendAsOwnerArgumentProvider() {
		return Stream.of(
			Arguments.of(true, "userId", "partyId"),
			Arguments.of(true, null, null),
			Arguments.of(false, "userId", null),
			Arguments.of(false, null, "partyId"));
	}

	private static Stream<Arguments> validSendAsOwnerArgumentProvider() {
		return Stream.of(
			Arguments.of(true, "userId", null),
			Arguments.of(true, null, "partyId"));

	}

}
