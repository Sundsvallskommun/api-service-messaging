package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;

@ExtendWith(MockitoExtension.class)
class ValidDigitalMailRequestConstraintValidatorTest {

	@InjectMocks
	private ValidDigitalMailRequestConstraintValidator validator;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private ConstraintValidatorContext mockContext;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private ConstraintValidatorContext.ConstraintViolationBuilder mockViolationBuilder;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext mockNodeBuilder;

	private static Stream<Arguments> validRequestProvider() {
		return Stream.of(
			Arguments.of("someBody", "text/plain"),
			Arguments.of(null, null),
			Arguments.of(null, "text/plain"));
	}

	@ParameterizedTest
	@MethodSource("validRequestProvider")
	void testValidRequest(final String body, final String contentType) {
		// Arrange
		final var request = createValidDigitalMailRequest()
			.withBody(body)
			.withContentType(contentType);

		// Act
		final var result = validator.isValid(request, mockContext);

		// Assert
		assertThat(result).isTrue();
		verifyNoInteractions(mockContext);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {
		"", "   "
	})
	void testInvalidRequest_WithBodyButInvalidContentType(final String contentType) {
		// Arrange
		final var request = createValidDigitalMailRequest()
			.withBody("someBody")
			.withContentType(contentType);
		when(mockContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(mockViolationBuilder);
		when(mockViolationBuilder.addPropertyNode(anyString())).thenReturn(mockNodeBuilder);
		when(mockNodeBuilder.addConstraintViolation()).thenReturn(mockContext);

		// Act
		final var result = validator.isValid(request, mockContext);

		// Assert
		assertThat(result).isFalse();
		verify(mockContext).disableDefaultConstraintViolation();
		verify(mockContext).buildConstraintViolationWithTemplate("contentType must be set when body is provided");
		verify(mockViolationBuilder).addPropertyNode("contentType");
		verify(mockNodeBuilder).addConstraintViolation();
	}

	@Test
	void testValidRequest_NullRequest() {
		// Act
		final var result = validator.isValid(null, mockContext);

		// Assert
		assertThat(result).isTrue();
		verifyNoInteractions(mockContext);
	}
}
