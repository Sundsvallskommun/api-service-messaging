package se.sundsvall.messaging.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncomingSmsRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void sender_shouldNotBeNullOrBlank() {
        IncomingSmsRequest nullSender = createRequest(sms -> sms.setSender(null));
        IncomingSmsRequest blankSender = createRequest(sms -> sms.setSender(" "));

        Set<ConstraintViolation<IncomingSmsRequest>> nullViolations = validator.validate(nullSender);
        Set<ConstraintViolation<IncomingSmsRequest>> blankViolations = validator.validate(blankSender);

        assertThat(nullViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("sender"));
        assertThat(blankViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("sender"));
    }

    @Test
    void mobileNumber_shouldStartWithAreaCode() {
        IncomingSmsRequest nullMobileNumber = createRequest(sms -> sms.setMobileNumber("0701234567"));

        Set<ConstraintViolation<IncomingSmsRequest>> violations = validator.validate(nullMobileNumber);

        assertThat(violations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must match"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("mobileNumber"));
    }

    @Test
    void mobileNumber_shouldBeElevenCharactersInLength() {
        IncomingSmsRequest tooShortNumber = createRequest(sms -> sms.setMobileNumber("+467012345678"));
        IncomingSmsRequest tooLongNumber = createRequest(sms -> sms.setMobileNumber("+467012345678"));

        Set<ConstraintViolation<IncomingSmsRequest>> shortViolations = validator.validate(tooShortNumber);
        Set<ConstraintViolation<IncomingSmsRequest>> longViolations = validator.validate(tooLongNumber);

        assertThat(shortViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must match"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("mobileNumber"));
        assertThat(longViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must match"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("mobileNumber"));
    }

    @Test
    void message_shouldNotBeNullOrBlank() {
        IncomingSmsRequest nullMessage = createRequest(sms -> sms.setMessage(null));
        IncomingSmsRequest blankMessage = createRequest(sms -> sms.setMessage(" "));

        Set<ConstraintViolation<IncomingSmsRequest>> nullViolations = validator.validate(nullMessage);
        Set<ConstraintViolation<IncomingSmsRequest>> blankViolations = validator.validate(blankMessage);

        assertThat(nullViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("message"));
        assertThat(blankViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("message"));
    }

    private IncomingSmsRequest createRequest(Consumer<IncomingSmsRequest> modifier) {
        IncomingSmsRequest smsRequest = IncomingSmsRequest.builder()
                .withPartyId(UUID.randomUUID().toString())
                .withSender("Sundsvall")
                .withMobileNumber("+46701234567")
                .withMessage("Message content")
                .build();

        if (modifier != null) {
            modifier.accept(smsRequest);
        }

        return smsRequest;
    }
}
