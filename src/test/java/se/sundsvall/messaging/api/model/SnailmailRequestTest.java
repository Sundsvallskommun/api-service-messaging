package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createSnailmailRequest;

import java.util.List;

import org.junit.jupiter.api.Test;

class SnailmailRequestTest extends AbstractValidationTest {

    @Test
    void testValidationWithValidRequest() {
        var request = createSnailmailRequest();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void testValidationWithInvalidPersonId() {
        var request = createSnailmailRequest(req -> req.setPersonId(null));

        var constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(2);
        assertThat(constraints).extracting("message").containsOnly("must not be blank", "not a valid UUID");

        request = createSnailmailRequest(req -> req.setPersonId("invalid"));

        constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("personId");
            assertThat(constraintViolation.getMessage()).startsWith("not a valid UUID");
        });
    }

    @Test
    void testValidationWithInvalidDepartment() {
        var request = createSnailmailRequest(req -> req.setDepartment(null));

        var constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("department");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
        request = createSnailmailRequest(req -> req.setDepartment(" "));

        constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("department");
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
        });
    }

    @Test
    void testValidationWithInvalidAttachment() {
        var request = createSnailmailRequest(req -> req.setAttachments(List.of(SnailmailRequest.Attachment.builder()
                .withName(null)
                .withContentType("someContentType")
                .withContent("someContent")
                .build())));

        var constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("attachments[0].name");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
        request = createSnailmailRequest(req -> req.setAttachments(List.of(SnailmailRequest.Attachment.builder()
                .withName("someName")
                .withContentType("someContentType")
                .withContent(null)
                .build())));

        constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("attachments[0].content");
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
        });
    }

}