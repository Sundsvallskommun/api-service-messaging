package se.sundsvall.messaging.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.messaging.api.validation.impl.ValidInstanceConstraintValidator;

@Documented
@Target({
	ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidInstanceConstraintValidator.class)
public @interface ValidInstance {

	String message() default "instance must be 'INTERNAL' or 'EXTERNAL'";

	boolean nullable();

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
