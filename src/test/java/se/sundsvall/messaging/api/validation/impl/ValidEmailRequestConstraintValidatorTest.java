package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.messaging.api.model.request.EmailRequest;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ValidEmailRequestConstraintValidatorTest {

	@InjectMocks
	private final ValidEmailRequestConstraintValidator validator = new ValidEmailRequestConstraintValidator();
	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private ConstraintValidatorContext mockContext;

	@Test
	void nullRequestIsValid() {
		assertThat(validator.isValid(null, mockContext)).isTrue();
	}

	@Test
	void requestWithEmailAddressIsValid() {
		final var request = EmailRequest.builder().withEmailAddress("recipient@recipient.se").build();

		assertThat(validator.isValid(request, mockContext)).isTrue();
	}

	@Test
	void requestWithRecipientsIsValid() {
		final var request = EmailRequest.builder().withRecipients(List.of("recipient@recipient.se")).build();

		assertThat(validator.isValid(request, mockContext)).isTrue();
	}

	@Test
	void requestWithoutAnyRecipientIsInvalid() {
		final var request = EmailRequest.builder().build();

		assertThat(validator.isValid(request, mockContext)).isFalse();
	}

	@Test
	void requestWithBlankRecipientsIsInvalid() {
		final var request = EmailRequest.builder().withEmailAddress(" ").withRecipients(List.of(" ")).build();

		assertThat(validator.isValid(request, mockContext)).isFalse();
	}
}
