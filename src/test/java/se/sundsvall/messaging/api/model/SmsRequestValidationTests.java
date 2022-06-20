package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createSmsRequest;

import java.util.List;

import org.junit.jupiter.api.Test;

class SmsRequestValidationTests extends AbstractValidationTest {

    @Test
    void testValidationWithValidRequest() {
        var request = createSmsRequest();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void testValidationWithInvalidPartyId() {
        var request = createSmsRequest(req -> req.getParty().setPartyId("not-a-uuid"));

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
        var request = createSmsRequest(req -> req.getParty().getExternalReferences().get(0).setKey(null));

        var constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("party.externalReferences[0].key");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });

        // Test invalid external reference value
        request = createSmsRequest(req -> req.getParty().getExternalReferences().get(0).setValue(null));

        constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("party.externalReferences[0].value");
            assertThat(constraintViolation.getMessage()).isEqualTo("must not be blank");
        });
    }

    @Test
    void testValidationWithInvalidMobileNumber() {
        var request = createSmsRequest(req -> req.setMobileNumber(null));

        var constraints = List.copyOf(validator.validate(request));

        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("mobileNumber");
            assertThat(constraintViolation.getMessage()).startsWith("must match");
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
