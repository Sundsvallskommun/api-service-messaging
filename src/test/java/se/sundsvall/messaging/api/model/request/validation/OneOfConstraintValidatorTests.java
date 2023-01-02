package se.sundsvall.messaging.api.model.request.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class OneOfConstraintValidatorTests {

    @Mock
    private OneOf mockOneOfAnnotation;
    @Mock
    private ConstraintValidatorContextImpl mockContext;

    private final OneOfConstraintValidator validator = new OneOfConstraintValidator();

    @Test
    void shouldFailWhenArgIsNull() {
        assertThat(validator.isValid(null, mockContext)).isFalse();

        verify(mockContext, times(1)).addMessageParameter(any(String.class), any());
    }

    @Test
    void shouldPassWhenArgIsNotNullAndValuesIsEmpty() {
        when(mockOneOfAnnotation.value()).thenReturn(new String[0]);

        validator.initialize(mockOneOfAnnotation);

        assertThat(validator.isValid("someString", mockContext)).isTrue();

        verify(mockContext, times(1)).addMessageParameter(any(String.class), any());
    }

    @Test
    void shouldFailWhenArgIsNotNullAndValuesDoesNotContainArg() {
        when(mockOneOfAnnotation.value()).thenReturn(new String[] { "someString", "someOtherString" });

        validator.initialize(mockOneOfAnnotation);

        assertThat(validator.isValid("someMissingString", mockContext)).isFalse();

        verify(mockContext, times(1)).addMessageParameter(any(String.class), any());
    }

    @Test
    void shouldPassWhenArgIsNotNullAndValuesContainsArg() {
        when(mockOneOfAnnotation.value()).thenReturn(new String[] { "someString", "someOtherString" });

        validator.initialize(mockOneOfAnnotation);

        assertThat(validator.isValid("someString", mockContext)).isTrue();

        verify(mockContext, times(1)).addMessageParameter(any(String.class), any());
    }
}
