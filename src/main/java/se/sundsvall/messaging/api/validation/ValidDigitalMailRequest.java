package se.sundsvall.messaging.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.messaging.api.validation.impl.ValidDigitalMailRequestConstraintValidator;

/**
 * Annotation for validating that if body is set in a digital mail request, contentType must also be set.
 */
@Documented
@Target({
	ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDigitalMailRequestConstraintValidator.class)
public @interface ValidDigitalMailRequest {

	String message() default "contentType must be set when body is provided";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
