package se.sundsvall.messaging.api.validation.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidNullOrNotEmptyConstraintValidatorTest {

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private ConstraintValidatorContext mockContext;

	@InjectMocks
	private final ValidNullOrNotEmptyValidator validator = new ValidNullOrNotEmptyValidator();

	@ParameterizedTest
	@ValueSource(strings = {
		"test", "Test (1)", "Test Test"
	})
	void validString(final String value) {
		assertThat(validator.isValid(value, mockContext)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " ", " test", "test ", " test ", "  test  "
	})
	void invalidString(final String value) {
		assertThat(validator.isValid(value, mockContext)).isFalse();
	}

	@Test
	void validNull() {
		assertThat(validator.isValid(null, mockContext)).isTrue();
	}
}
