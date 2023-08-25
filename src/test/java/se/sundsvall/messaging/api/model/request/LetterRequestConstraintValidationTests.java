package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidLetterRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.LetterRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class LetterRequestConstraintValidationTests {

    private final LetterRequest validRequest = createValidLetterRequest();

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
        var request = validRequest.withParty(validRequest.party().withPartyIds(List.of("not-a-uuid")));

        assertThat(request)
            .hasSingleConstraintViolation("party.partyIds[0].<list element>", "not a valid UUID");
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
    void shouldPassWithNullSender() {
        assertThat(validRequest.withSender(null)).hasNoConstraintViolations();
    }

    @Test
    void shouldFailWithNullSenderSupportInfo() {
        var sender = validRequest.sender().withSupportInfo(null);

        assertThat(validRequest.withSender(sender))
            .hasSingleConstraintViolation("sender.supportInfo", "must not be null");
    }

    @Test
    void shouldFailWithSendWithSupportInfoWithNullText() {
        var sender = validRequest.sender()
            .withSupportInfo(validRequest.sender().supportInfo().withText(null));

        assertThat(validRequest.withSender(sender))
            .hasSingleConstraintViolation("sender.supportInfo.text", "must not be blank");
    }

    @Test
    void shouldFailWithSendWithSupportInfoWithBlankText() {
        var sender = validRequest.sender()
            .withSupportInfo(validRequest.sender().supportInfo().withText(" "));

        assertThat(validRequest.withSender(sender))
            .hasSingleConstraintViolation("sender.supportInfo.text", "must not be blank");
    }

    @Test
    void shouldFailWithSendWithSupportInfoWithNullEmailAddress() {
        var sender = validRequest.sender()
            .withSupportInfo(validRequest.sender().supportInfo().withEmailAddress(null));

        assertThat(validRequest.withSender(sender))
            .hasSingleConstraintViolation("sender.supportInfo.emailAddress", "must not be blank");
    }

    @Test
    void shouldFailWithSendWithSupportInfoWithBlankEmailAddress() {
        var sender = validRequest.sender()
            .withSupportInfo(validRequest.sender().supportInfo().withEmailAddress(" "));

        assertThat(validRequest.withSender(sender))
            .hasConstraintViolation("sender.supportInfo.emailAddress", "must not be blank")
            .hasConstraintViolation("sender.supportInfo.emailAddress", "must be a well-formed email address");
    }

    @Test
    void shouldFailWithSendWithSupportInfoWithInvalidEmailAddress() {
        var sender = validRequest.sender()
            .withSupportInfo(validRequest.sender().supportInfo().withEmailAddress("not-an-email-address"));

        assertThat(validRequest.withSender(sender))
            .hasSingleConstraintViolation("sender.supportInfo.emailAddress", "must be a well-formed email address");
    }

    @Test
    void shouldFailWithSendWithSupportInfoWithNullPhoneNumber() {
        var sender = validRequest.sender()
            .withSupportInfo(validRequest.sender().supportInfo().withPhoneNumber(null));

        assertThat(validRequest.withSender(sender))
            .hasSingleConstraintViolation("sender.supportInfo.phoneNumber", "must not be blank");
    }

    @Test
    void shouldFailWithSendWithSupportInfoWithBlankPhoneNumber() {
        var sender = validRequest.sender()
            .withSupportInfo(validRequest.sender().supportInfo().withPhoneNumber(" "));

        assertThat(validRequest.withSender(sender))
            .hasSingleConstraintViolation("sender.supportInfo.phoneNumber", "must not be blank");
    }

    @Test
    void shouldFailWithSendWithSupportInfoWithNullUrl() {
        var sender = validRequest.sender()
            .withSupportInfo(validRequest.sender().supportInfo().withUrl(null));

        assertThat(validRequest.withSender(sender))
            .hasSingleConstraintViolation("sender.supportInfo.url", "must not be blank");
    }

    @Test
    void shouldFailWithSendWithSupportInfoWithBlankUrl() {
        var sender = validRequest.sender()
            .withSupportInfo(validRequest.sender().supportInfo().withUrl(" "));

        assertThat(validRequest.withSender(sender))
            .hasSingleConstraintViolation("sender.supportInfo.url", "must not be blank");
    }

    @Test
    void shouldFailWithInvalidContentType() {
        assertThat(validRequest.withContentType("invalid-content-type"))
            .hasSingleConstraintViolation("contentType", "must be one of: [text/plain, text/html]");
    }
}
