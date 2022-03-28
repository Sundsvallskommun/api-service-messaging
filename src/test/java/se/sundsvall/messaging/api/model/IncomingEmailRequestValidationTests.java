package se.sundsvall.messaging.api.model;

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

class IncomingEmailRequestValidationTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testValidationWithNoRecipientEmail() {
        var blankEmailAddress = createIncomingEmailRequest(request -> request.setEmailAddress(""));
        var nullEmailAddress = createIncomingEmailRequest(request -> request.setEmailAddress(null));
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
        var blankSubject = createIncomingEmailRequest(request -> request.setSubject(""));
        var nullSubject = createIncomingEmailRequest(request -> request.setSubject(null));

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
        var blankSender = createIncomingEmailRequest(request -> request.setSubject(""));
        var nullSender = createIncomingEmailRequest(request -> request.setSubject(null));

        var blankConstraints = List.copyOf(validator.validate(blankSender));
        var nullConstraints = List.copyOf(validator.validate(nullSender));

        assertThat(blankConstraints).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().equals("subject"));
        assertThat(nullConstraints).hasSize(1)
            .allMatch(constraint -> constraint.getMessage().contains("must not be blank"))
            .allMatch(constraint -> constraint.getPropertyPath().toString().equals("subject"));
    }

    private EmailRequest createIncomingEmailRequest(final Consumer<EmailRequest> modifier) {
        var request = EmailRequest.builder()
            .withEmailAddress("test@hotmail.com")
            .withAttachments(List.of(EmailRequest.Attachment.builder()
                .withContent("content")
                .withContentType("contentType")
                .withName("name")
                .build()))
            .withHtmlMessage("HTML")
            .withMessage("message")
            .withParty(Party.builder()
                .withPartyId(UUID.randomUUID().toString())
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withSenderEmail("sender@hotmail.com")
            .withSenderName("sender")
            .withSubject("subject")
            .build();

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }
}
