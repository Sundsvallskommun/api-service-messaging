package se.sundsvall.messaging.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import se.sundsvall.messaging.api.validation.impl.ValidAttachmentConstraintValidator;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = ValidAttachmentConstraintValidator.class)
public @interface ValidAttachment {

	String message() default "either content or objectId must be set, but not both";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
