package se.sundsvall.messaging.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncomingEmailRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidationWithNoRecipientEmail() {
        IncomingEmailRequest blankEmailAddress = createIncomingEmailRequest(request -> request.setEmailAddress(""));
        IncomingEmailRequest nullEmailAddress = createIncomingEmailRequest(request -> request.setEmailAddress(null));
        var blankConstraints = List.copyOf(validator.validate(blankEmailAddress));
        var nullConstraints = List.copyOf(validator.validate(nullEmailAddress));

        assertThat(blankConstraints).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("emailAddress"));
        assertThat(nullConstraints).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("emailAddress"));
    }

    @Test
    void testValidationWithNoSubject() {
        IncomingEmailRequest blankSubject = createIncomingEmailRequest(request -> request.setSubject(""));
        IncomingEmailRequest nullSubject = createIncomingEmailRequest(request -> request.setSubject(null));

        var blankConstraints = List.copyOf(validator.validate(blankSubject));
        var nullConstraints = List.copyOf(validator.validate(nullSubject));

        assertThat(blankConstraints).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("subject"));
        assertThat(nullConstraints).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("subject"));
    }

    @Test
    void testValidationWithNoSender() {
        IncomingEmailRequest blankSender = createIncomingEmailRequest(request -> request.setSubject(""));
        IncomingEmailRequest nullSender = createIncomingEmailRequest(request -> request.setSubject(null));

        var blankConstraints = List.copyOf(validator.validate(blankSender));
        var nullConstraints = List.copyOf(validator.validate(nullSender));

        assertThat(blankConstraints).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("subject"));
        assertThat(nullConstraints).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("subject"));
    }

    @Test
    void testValidationWithNonValidSenderEmail() {
        IncomingEmailRequest blankSenderEmail = createIncomingEmailRequest(request -> request.setSenderEmail(""));

        IncomingEmailRequest nullSenderEmail = createIncomingEmailRequest(request -> request.setSenderEmail(null));

        IncomingEmailRequest nonValidSenderEmail = createIncomingEmailRequest(request -> request.setSenderEmail("test"));

        var blankConstraints = List.copyOf(validator.validate(blankSenderEmail));
        var nullConstraints = List.copyOf(validator.validate(nullSenderEmail));
        var nonValidConstraints = List.copyOf(validator.validate(nonValidSenderEmail));

        assertThat(blankConstraints).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("senderEmail"));
        assertThat(nullConstraints).hasSize(1).allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("senderEmail"));
        assertThat(nonValidConstraints).hasSize(1)
                .allMatch(constraint -> constraint.getMessage().contains("must be a well-formed email address"))
                .allMatch(constraint -> constraint.getPropertyPath().toString().equals("senderEmail"));
    }

    private IncomingEmailRequest createIncomingEmailRequest(Consumer<IncomingEmailRequest> modifier) {
        IncomingEmailRequest incomingEmailRequest = IncomingEmailRequest.builder()
                .withEmailAddress("test@hotmail.com")
                .withAttachments(List.of(IncomingEmailRequest.Attachment.builder()
                        .withContent("content")
                        .withContentType("contentType")
                        .withName("name")
                        .build()))
                .withHtmlMessage("HTML")
                .withMessage("message")
                .withPartyId(UUID.randomUUID().toString())
                .withSenderEmail("sender@hotmail.com")
                .withSenderName("sender")
                .withSubject("subject")
                .build();

        if (modifier != null) {
            modifier.accept(incomingEmailRequest);
        }
        return incomingEmailRequest;
    }
}
