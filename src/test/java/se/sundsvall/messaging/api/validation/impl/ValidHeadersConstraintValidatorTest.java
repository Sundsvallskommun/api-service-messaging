package se.sundsvall.messaging.api.validation.impl;

import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintViolationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.messaging.api.model.request.Header;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidHeadersConstraintValidatorTest {

	@Mock
	private HibernateConstraintValidatorContext mockContext;
	@Mock
	private HibernateConstraintViolationBuilder mockViolationBuilder;
	@Mock
	private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext mockNodeBuilderContext;

	private final ValidHeadersConstraintValidator validator = new ValidHeadersConstraintValidator();

	@ParameterizedTest
	@NullAndEmptySource
	void nullAndEmptyHeaderMap(final Map<?, ?> headers) {
		assertThat(validator.isValid(headers, mockContext)).isTrue();

		verifyNoInteractions(mockContext);
	}

	@Test
	void headerMapWithValidHeadersOnlyUsingEnumConstants() {
		when(mockContext.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(mockContext);

		var headers = Map.of(
			Header.MESSAGE_ID.name(), List.of("<aaa@bbb>"),
			Header.IN_REPLY_TO.name(), List.of("<ccc@ddd>"),
			Header.REFERENCES.name(), List.of("<eee@fff>"),
			Header.AUTO_SUBMITTED.name(), List.of("auto-generated"));

		assertThat(validator.isValid(headers, mockContext)).isTrue();

		verify(mockContext).unwrap(HibernateConstraintValidatorContext.class);
		verify(mockContext).disableDefaultConstraintViolation();
		verifyNoMoreInteractions(mockContext);
	}

	@Test
	void headerMapWithValidHeadersOnlyUsingEnumConstantKeys() {
		when(mockContext.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(mockContext);

		var headers = Map.of(
			Header.MESSAGE_ID.getKey(), List.of("<aaa@bbb>"),
			Header.IN_REPLY_TO.getKey(), List.of("<ccc@ddd>"),
			Header.REFERENCES.getKey(), List.of("<eee@fff>"),
			Header.AUTO_SUBMITTED.name(), List.of("auto-generated"));

		assertThat(validator.isValid(headers, mockContext)).isTrue();

		verify(mockContext).unwrap(HibernateConstraintValidatorContext.class);
		verify(mockContext).disableDefaultConstraintViolation();
		verifyNoMoreInteractions(mockContext);
	}

	@Test
	void headerMapWithInvalidHeaderNames() {
		when(mockContext.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(mockContext);
		when(mockContext.buildConstraintViolationWithTemplate(any())).thenReturn(mockViolationBuilder);
		when(mockViolationBuilder.addPropertyNode(any())).thenReturn(mockNodeBuilderContext);
		when(mockNodeBuilderContext.addConstraintViolation()).thenReturn(mockContext);

		var headers = Map.of(
			"SomeUnknownHeader", List.of("someValue"),
			"SomeOtherUnknownHeader", List.of("someOtherValue"));

		assertThat(validator.isValid(headers, mockContext)).isFalse();

		verify(mockContext).unwrap(HibernateConstraintValidatorContext.class);
		verify(mockContext).disableDefaultConstraintViolation();
		verify(mockContext, times(2)).buildConstraintViolationWithTemplate(any());
		verifyNoMoreInteractions(mockContext);
	}

	@Test
	void headerMapWithValidHeaderNamesAndInvalidHeaderValues() {
		when(mockContext.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(mockContext);
		when(mockContext.buildConstraintViolationWithTemplate(any())).thenReturn(mockViolationBuilder);
		when(mockViolationBuilder.addPropertyNode(any())).thenReturn(mockNodeBuilderContext);
		when(mockNodeBuilderContext.addConstraintViolation()).thenReturn(mockContext);

		var headers = Map.of(
			Header.MESSAGE_ID.name(), List.of("invalid-message-id-value"),
			Header.REFERENCES.name(), List.of("invalid-references-value"),
			Header.IN_REPLY_TO.name(), List.of("invalid-in-reply-to-value"),
			Header.AUTO_SUBMITTED.name(), List.of("invalid-auto-submitted-value"));

		assertThat(validator.isValid(headers, mockContext)).isFalse();

		verify(mockContext).unwrap(HibernateConstraintValidatorContext.class);
		verify(mockContext).disableDefaultConstraintViolation();
		verify(mockContext, times(4)).buildConstraintViolationWithTemplate(any());
		verifyNoMoreInteractions(mockContext);
	}
}
