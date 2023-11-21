package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidDigitalInvoiceRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.DigitalInvoiceRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class DigitalInvoiceRequestConstraintValidationTests {

    private final DigitalInvoiceRequest validRequest = createValidDigitalInvoiceRequest();

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
    void shouldFailWithNullInvoiceType() {
        assertThat(validRequest.withType(null)).hasSingleConstraintViolation("type", "must not be null");
    }

    @Test
    void shouldFailWithNullDetails() {
        assertThat(validRequest.withDetails(null)).hasSingleConstraintViolation("details", "must not be null");
    }

    @Test
    void shouldFailWithDetailsWithNullAmount() {
        var details = validRequest.details().withAmount(null);

        assertThat(validRequest.withDetails(details)).hasSingleConstraintViolation("details.amount", "must not be null");
    }

    @ParameterizedTest
    @ValueSource(floats = {0.0f, -12.34f})
    void shouldFailWithDetailsWithNonPositiveAmount(final float amount) {
        var details = validRequest.details().withAmount(amount);

        assertThat(validRequest.withDetails(details)).hasSingleConstraintViolation("details.amount", "must be greater than 0");
    }

    @Test
    void shouldFailWithDetailsWithNullDueDate() {
        var details = validRequest.details().withDueDate(null);

        assertThat(validRequest.withDetails(details)).hasSingleConstraintViolation("details.dueDate", "must not be null");
    }

    @Test
    void shouldFailWithDetailsWithNullPaymentReferenceType() {
        var details = validRequest.details().withPaymentReferenceType(null);

        assertThat(validRequest.withDetails(details)).hasSingleConstraintViolation("details.paymentReferenceType", "must not be null");
    }

    @Test
    void shouldFailWithDetailsWithNullPaymentReference() {
        var details = validRequest.details().withPaymentReference(null);

        assertThat(validRequest.withDetails(details)).hasSingleConstraintViolation("details.paymentReference", "must not be blank");
    }

    @Test
    void shouldFailWithDetailsWithBlankPaymentReference() {
        var details = validRequest.details().withPaymentReference("");

        assertThat(validRequest.withDetails(details)).hasSingleConstraintViolation("details.paymentReference", "must not be blank");
    }

    @Test
    void shouldFailWithDetailsWithNullAccountType() {
        var details = validRequest.details().withAccountType(null);

        assertThat(validRequest.withDetails(details)).hasSingleConstraintViolation("details.accountType", "must not be null");
    }

    @Test
    void shouldFailWithDetailsWithNullAccountNumber() {
        var details = validRequest.details().withAccountNumber(null);

        assertThat(validRequest.withDetails(details)).hasSingleConstraintViolation("details.accountNumber", "must not be blank");
    }

    @Test
    void shouldFailWithDetailsWithBlankAccountNumber() {
        var details = validRequest.details().withAccountNumber("");

        assertThat(validRequest.withDetails(details)).hasSingleConstraintViolation("details.accountNumber", "must not be blank");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "text/plain"})
    void shouldFailWithFileWithInvalidContentType(final String contentType) {
        var file = validRequest.files().get(0).withContentType(contentType);

        assertThat(validRequest.withFiles(List.of(file))).hasSingleConstraintViolation("files[0].contentType", "must be one of: [application/pdf]");
    }

    @Test
    void shouldFailWithFileWithNullContent() {
        var file = validRequest.files().get(0).withContent(null);

        assertThat(validRequest.withFiles(List.of(file))).hasSingleConstraintViolation("files[0].content", "not a valid BASE64-encoded string");
    }

    @Test
    @Disabled("Disabled until the BASE64-validation logic for blank strings is released in dept44")
    void shouldFailWithFileWithBlankContent() {
        var file = validRequest.files().get(0).withContent("");

        assertThat(validRequest.withFiles(List.of(file))).hasSingleConstraintViolation("files[0].content", "not a valid BASE64-encoded string");
    }

    @Test
    void shouldFailWithFileWithInvalidContent() {
        var file = validRequest.files().get(0).withContent("___abc123");

        assertThat(validRequest.withFiles(List.of(file))).hasSingleConstraintViolation("files[0].content", "not a valid BASE64-encoded string");
    }

    @Test
    void shouldFailWithFileWithNullFilename() {
        var file = validRequest.files().get(0).withFilename(null);

        assertThat(validRequest.withFiles(List.of(file))).hasSingleConstraintViolation("files[0].filename", "must not be blank");
    }

    @Test
    void shouldFailWithFileWithBlankFilename() {
        var file = validRequest.files().get(0).withFilename("");

        assertThat(validRequest.withFiles(List.of(file))).hasSingleConstraintViolation("files[0].filename", "must not be blank");
    }
}
