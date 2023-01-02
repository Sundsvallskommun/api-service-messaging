package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidSnailMailRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.SnailMailRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SnailMailRequestConstraintValidationTests {

    private final SnailMailRequest validRequest = createValidSnailMailRequest();

    @Test
    void shouldPassForValidRequest() {
        assertThat(validRequest).hasNoConstraintViolations();
    }

    @Test
    void shouldPassWithNullParty() {
        assertThat(validRequest.withParty(null)).hasNoConstraintViolations();
    }

    @Test
    void shouldFailWithInvalidPartyId() {
        var request = validRequest.withParty(validRequest.party().withPartyId("not-a-uuid"));

        assertThat(request).hasSingleConstraintViolation("party.partyId", "not a valid UUID");
    }
    @Test
    void shouldFailWithInvalidExternalReference() {
        var externalReference  = validRequest.party().externalReferences().get(0);

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
    void shouldFailWithNullDepartment() {
        assertThat(validRequest.withDepartment(null))
            .hasSingleConstraintViolation("department", "must not be blank");
    }

    @Test
    void shouldFailWithBlankDepartment() {
        assertThat(validRequest.withDepartment(" "))
            .hasSingleConstraintViolation("department", "must not be blank");
    }

    @Test
    void shouldPassWithNullDeviation() {
        assertThat(validRequest.withDeviation(null)).hasNoConstraintViolations();
    }

    @Test
    void shouldPassWithBlankDeviation() {
        assertThat(validRequest.withDeviation(" ")).hasNoConstraintViolations();
    }

    @Test
    void shouldPassWithNullAttachments() {
        assertThat(validRequest.withAttachments(null)).hasNoConstraintViolations();
    }

    @Test
    void shouldPassWithEmptyAttachments() {
        assertThat(validRequest.withAttachments(List.of())).hasNoConstraintViolations();
    }

    @Test
    void shouldFailWithAttachmentWithNullName() {
        var request = validRequest.withAttachments(List.of(validRequest.attachments().get(0).withName(null)));

        assertThat(request)
            .hasSingleConstraintViolation("attachments[0].name", "must not be blank");
    }

    @Test
    void shouldFailWithAttachmentWithBlankName() {
        var request = validRequest.withAttachments(List.of(validRequest.attachments().get(0).withName(" ")));

        assertThat(request)
            .hasSingleConstraintViolation("attachments[0].name", "must not be blank");
    }

    @Test
    void shouldFailWithAttachmentWithNullContent() {
        var request = validRequest.withAttachments(List.of(validRequest.attachments().get(0).withContent(null)));

        assertThat(request)
            .hasSingleConstraintViolation("attachments[0].content", "not a valid BASE64-encoded string");
    }

    @Test
    void shouldFailWithAttachmentWithInvalidContent() {
        var request = validRequest.withAttachments(List.of(validRequest.attachments().get(0).withContent("not-base-64!!!")));

        assertThat(request)
            .hasSingleConstraintViolation("attachments[0].content", "not a valid BASE64-encoded string");
    }
}
