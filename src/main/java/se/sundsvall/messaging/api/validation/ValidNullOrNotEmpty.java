package se.sundsvall.messaging.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.messaging.api.validation.impl.ValidNullOrNotEmptyValidator;

/**
 * Annotation for validating that a string is either null or not empty and has no starting or trailing spaces.
 */
@Documented
@Target({
	ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidNullOrNotEmptyValidator.class)
public @interface ValidNullOrNotEmpty {

	String message() default "text is not null or not empty or has starting or trailing spaces";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
