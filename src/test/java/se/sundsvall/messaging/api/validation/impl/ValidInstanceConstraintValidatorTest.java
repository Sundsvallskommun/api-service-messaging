package se.sundsvall.messaging.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.api.validation.ValidInstance;

@ExtendWith(MockitoExtension.class)
class ValidInstanceConstraintValidatorTest {

	@Mock
	private ValidInstance mockAnnotation;

	@InjectMocks
	private ValidInstanceConstraintValidator validator;

	@ParameterizedTest
	@ValueSource(strings = {
		"internal", "external"
	})
	void validInstance(final String oepInstance) {
		validator.initialize(mockAnnotation);

		assertThat(validator.isValid(oepInstance)).isTrue();
		assertThat(validator.isValid(oepInstance, null)).isTrue();

		verify(mockAnnotation).nullable();
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {
		"not-valid", "", " "
	})
	void invalidOepInstance(final String oepInstance) {
		validator.initialize(mockAnnotation);

		assertThat(validator.isValid(oepInstance)).isFalse();
		assertThat(validator.isValid(oepInstance, null)).isFalse();

		verify(mockAnnotation).nullable();
	}

	@Test
	void nullOepInstanceWhenNullableIsTrue() {
		when(mockAnnotation.nullable()).thenReturn(true);
		validator.initialize(mockAnnotation);

		assertThat(validator.isValid(null)).isTrue();
		assertThat(validator.isValid(null, null)).isTrue();
		assertThat(validator.isValid("not-valid")).isFalse();
		assertThat(validator.isValid("not-valid", null)).isFalse();

		verify(mockAnnotation).nullable();
	}

}
