package se.sundsvall.messaging.api.model.request.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.messagingrules.HeaderName;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ValidHeaderNameConstraintValidatorTests {

    @Mock
    private ConstraintValidatorContextImpl mockContext;

    private final ValidHeaderNameConstraintValidator validator = new ValidHeaderNameConstraintValidator();

    @Test
    void shouldFailWhenArgIsNull() {
        assertThat(validator.isValid(null, mockContext)).isFalse();

        verify(mockContext, times(1)).addMessageParameter(any(String.class), any());
    }

    @Test
    void shouldPassWhenArgIsNotNull() {
        assertThat(validator.isValid(HeaderName.TYPE, mockContext)).isTrue();

        verify(mockContext, never()).addMessageParameter(any(String.class), any());
    }
}
