package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createSmsRequest;

import java.util.List;

import org.junit.jupiter.api.Test;

class EmailRequestValidationTests extends AbstractValidationTest {

    @Test
    void testValidationWithValidRequest() {
        var request = createEmailRequest();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void testValidationWithInvalidPartyId() {
        var request = createEmailRequest(req -> req.getParty().setPartyId("not-a-uuid"));

        var constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("party.partyId");
            assertThat(constraintViolation.getMessage()).isEqualTo("not a valid UUID");
        });
    }

    @Test
    void testValidationWithInvalidExternalReference() {
        // Test invalid external reference key
        var request = createEmailRequest(req -> req.getParty().getExternalReferences().get(0).setKey(null));

        var constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("party.externalReferences[0].key");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });

        // Test invalid external reference value
        request = createEmailRequest(req -> req.getParty().getExternalReferences().get(0).setValue(null));

        constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("party.externalReferences[0].value");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
    }

    @Test
    void testValidationWithInvalidEmailAddress() {
        var request = createEmailRequest(req -> req.setEmailAddress(null));

        var constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("emailAddress");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });

        request = createEmailRequest(req -> req.setEmailAddress("invalid"));

        constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("emailAddress");
            assertThat(constraintViolation.getMessage()).startsWith("must be a well-formed email address");
        });
    }

    @Test
    void testValidationWithInvalidMessage() {
        var request = createSmsRequest(req -> req.setMessage(null));

        var constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("message");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
    }
}
