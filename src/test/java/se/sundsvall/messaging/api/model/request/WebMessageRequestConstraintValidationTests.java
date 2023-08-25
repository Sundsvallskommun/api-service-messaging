package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.WebMessageRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class WebMessageRequestConstraintValidationTests {

    private final WebMessageRequest validRequest = createValidWebMessageRequest();

    @Test
    void shouldPassForValidRequest() {
        assertThat(validRequest).hasNoConstraintViolations();
    }

    @Test
    void shouldFailWithNullParty() {
        assertThat(validRequest.withParty(null)).hasSingleConstraintViolation("party", "must not be null");
    }

    @Test
    void shouldFailWithInvalidPartyId() {
        var request = validRequest.withParty(validRequest.party().withPartyId("not-a-uuid"));

        assertThat(request)
            .hasSingleConstraintViolation("party.partyId", "not a valid UUID");
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
    void shouldFailWithNullMessage() {
        assertThat(validRequest.withMessage(null))
            .hasSingleConstraintViolation("message", "must not be blank");
    }

    @Test
    void shouldFailWithBlankMessage() {
        assertThat(validRequest.withMessage(" "))
            .hasSingleConstraintViolation("message", "must not be blank");
    }

    @Test
    void shouldPassWithNullAttachments() {
        assertThat(validRequest.withAttachments(null)).hasNoConstraintViolations();
    }

    @Test
    void shouldPassWithEmptyAttachments() {
        assertThat(validRequest.withAttachments(List.of())).hasNoConstraintViolations();
    }

    // TODO: what are the requirements for WebMessage attachments ???
}
