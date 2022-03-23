package se.sundsvall.messaging.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void partyId_shouldNotBeNullOrBlank() {
        MessageRequest nullPartyId = createRequest(message -> message.setPartyId(null));
        MessageRequest blankPartyId = createRequest(message -> message.setPartyId(" "));

        Set<ConstraintViolation<MessageRequest>> nullViolations = validator.validate(nullPartyId);
        Set<ConstraintViolation<MessageRequest>> blankViolations = validator.validate(blankPartyId);

        assertThat(nullViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().contains("partyId"));
        assertThat(blankViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().contains("partyId"));
    }

    @Test
    void message_shouldNotBeNullOrBlank() {
        MessageRequest nullMessage = createRequest(message -> message.setMessage(null));
        MessageRequest blankMessage = createRequest(message -> message.setMessage(" "));

        Set<ConstraintViolation<MessageRequest>> nullViolations = validator.validate(nullMessage);
        Set<ConstraintViolation<MessageRequest>> blankViolations = validator.validate(blankMessage);

        assertThat(nullViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().contains("message"));
        assertThat(blankViolations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().contains("message"));
    }

    @Test
    void smsName_shouldBeMaxElevenCharacters() {
        MessageRequest tooLongSmsName = createRequest(message -> message.setSmsName("Sundsvalls Kommun"));

        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(tooLongSmsName);

        assertThat(violations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must be between 0 and 11"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().contains("smsName"));
    }

    @Test
    void senderEmail_shouldBeWellFormedEmail() {
        MessageRequest noAtSign = createRequest(message -> message.setSenderEmail("noreplysundsvall.se"));

        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(noAtSign);

        assertThat(violations).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must be a well-formed email"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().contains("senderEmail"));
    }

    private MessageRequest createRequest(Consumer<MessageRequest.Message> modifier) {
        MessageRequest.Message message = MessageRequest.Message.builder()
                .withPartyId(UUID.randomUUID().toString())
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
