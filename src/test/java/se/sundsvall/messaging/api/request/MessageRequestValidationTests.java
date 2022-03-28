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

class MessageRequestValidationTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void partyId_shouldNotBeNullOrBlank() {
        var nullPartyId = createRequest(message -> message.getParty().setPartyId(null));
        var blankPartyId = createRequest(message -> message.getParty().setPartyId(" "));

        var nullViolations = validator.validate(nullPartyId);
        var blankViolations = validator.validate(blankPartyId);

        assertThat(nullViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().contains("partyId"));
        assertThat(blankViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().contains("partyId"));
    }

    @Test
    void message_shouldNotBeNullOrBlank() {
        var nullMessage = createRequest(message -> message.setMessage(null));
        var blankMessage = createRequest(message -> message.setMessage(" "));

        var nullViolations = validator.validate(nullMessage);
        var blankViolations = validator.validate(blankMessage);

        assertThat(nullViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().contains("message"));
        assertThat(blankViolations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().contains("message"));
    }

    @Test
    void smsName_shouldBeMaxElevenCharacters() {
        var tooLongSmsName = createRequest(message -> message.setSmsName("Sundsvalls Kommun"));

        var violations = validator.validate(tooLongSmsName);

        assertThat(violations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must be between 0 and 11"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().contains("smsName"));
    }

    @Test
    void senderEmail_shouldBeWellFormedEmail() {
        var noAtSign = createRequest(message -> message.setSenderEmail("noreplysundsvall.se"));

        var violations = validator.validate(noAtSign);

        assertThat(violations).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must be a well-formed email"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().contains("senderEmail"));
    }

    private MessageRequest createRequest(Consumer<MessageRequest.Message> modifier) {
        var message = MessageRequest.Message.builder()
            .withParty(Party.builder()
                .withPartyId(UUID.randomUUID().toString())
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withEmailName("Sundsvalls Kommun")
            .withSmsName("Sundsvall")
            .withSenderEmail("noreply@sundsvall.se")
            .withSubject("Message subject")
            .withMessage("Message content")
            .build();

        if (modifier != null) {
            modifier.accept(message);
        }

        return MessageRequest.builder()
            .withMessages(List.of(message))
            .build();
    }
}
