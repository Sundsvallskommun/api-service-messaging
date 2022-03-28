package se.sundsvall.messaging.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Party;

class IncomingSmsRequestValidationTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void sender_shouldNotBeNullOrBlank() {
        var nullSender = createRequest(sms -> sms.setSender(null));
        var blankSender = createRequest(sms -> sms.setSender(" "));

        var nullViolations = validator.validate(nullSender);
        var blankViolations = validator.validate(blankSender);

        assertThat(nullViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().equals("sender"));
        assertThat(blankViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().equals("sender"));
    }

    @Test
    void mobileNumber_shouldStartWithAreaCode() {
        var nullMobileNumber = createRequest(sms -> sms.setMobileNumber("0701234567"));

        var violations = validator.validate(nullMobileNumber);

        assertThat(violations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must match"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().equals("mobileNumber"));
    }

    @Test
    void mobileNumber_shouldBeElevenCharactersInLength() {
        var tooShortNumber = createRequest(sms -> sms.setMobileNumber("+467012345678"));
        var tooLongNumber = createRequest(sms -> sms.setMobileNumber("+467012345678"));

        var shortViolations = validator.validate(tooShortNumber);
        var longViolations = validator.validate(tooLongNumber);

        assertThat(shortViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must match"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().equals("mobileNumber"));
        assertThat(longViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must match"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().equals("mobileNumber"));
    }

    @Test
    void message_shouldNotBeNullOrBlank() {
        var nullMessage = createRequest(sms -> sms.setMessage(null));
        var blankMessage = createRequest(sms -> sms.setMessage(" "));

        var nullViolations = validator.validate(nullMessage);
        var blankViolations = validator.validate(blankMessage);

        assertThat(nullViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().equals("message"));
        assertThat(blankViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().equals("message"));
    }

    private IncomingSmsRequest createRequest(Consumer<IncomingSmsRequest> modifier) {
        var request = IncomingSmsRequest.builder()
            .withParty(Party.builder()
                .withPartyId(UUID.randomUUID().toString())
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withSender("Sundsvall")
            .withMobileNumber("+46701234567")
            .withMessage("Message content")
            .build();

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }
}
