package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.SmsRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SmsRequestConstraintValidationTests {

    private final SmsRequest validRequest = createValidSmsRequest();

    @Test
    void shouldPassForValidRequest() {
        assertThat(validRequest).hasNoConstraintViolations();
    }

    @Test
    void shouldPassWithoutParty() {
        assertThat(validRequest.withParty(null)).hasNoConstraintViolations();
    }

    @Test
    void shouldFailWithInvalidPartyId() {
        var request = validRequest.withParty(validRequest.party().withPartyId("not-a-uuid"));

        assertThat(request).hasSingleConstraintViolation("party.partyId", "not a valid UUID");
    }

    @Test
    void shouldFailWithInvalidExternalReference() {
        var externalReference = validRequest.party().externalReferences().get(0);

        // Test invalid external reference key
        var request = validRequest.withParty(validRequest.party()
            .withExternalReferences(List.of(externalReference.withKey(null))));

        assertThat(request)
            .hasSingleConstraintViolation("party.externalReferences[0].key", "must not be blank");

        // Test invalid external reference value
        request = validRequest.withParty(validRequest.party()
            .withExternalReferences(List.of(externalReference.withValue(null))));

        assertThat(request)
            .hasSingleConstraintViolation("party.externalReferences[0].value", "must not be blank");
    }

    @Test
    void shouldFailWithInvalidHeaders() {
        var header = validRequest.headers().get(0);

        // Test null header name
        var request = validRequest.withHeaders(List.of(header.withName(null)));

        assertThat(request)
            .hasSingleConstraintViolation("headers[0].name", message -> message.startsWith("must be one of"));

        // Test empty (null) header values
        request = validRequest.withHeaders(List.of(header.withValues(null)));

        assertThat(request)
            .hasSingleConstraintViolation("headers[0].values", "must not be empty");
    }

    @Test
    void shouldFailWithNullMobileNumber() {
        assertThat(validRequest.withMobileNumber(null))
            .hasSingleConstraintViolation("mobileNumber", message -> message.startsWith("must match"));
    }

    @Test
    void shouldFailWithInvalidMobileNumber() {
        assertThat(validRequest.withMobileNumber("not-a-mobile-number"))
            .hasSingleConstraintViolation("mobileNumber", message -> message.startsWith("must match"));
    }

    @Test
    void shouldFailWithNullMessage() {
        assertThat(validRequest.withMessage(null))
            .hasSingleConstraintViolation("message", "must not be blank");
    }

    @Test
    void shouldFailWithBlankMessage() {
        assertThat(validRequest.withMessage(""))
            .hasSingleConstraintViolation("message", "must not be blank");
    }
}
