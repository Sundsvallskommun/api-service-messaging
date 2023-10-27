package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidMessageRequestMessage;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.MessageRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class MessageRequestConstraintValidationTests {

    private final MessageRequest.Message validMessage = createValidMessageRequestMessage();

    @Test
    void shouldPassForValidRequest() {
        assertThat(createMessageRequest(validMessage)).hasNoConstraintViolations();
    }

    @Test
    void shouldFailWithNullParty() {
        var message = validMessage.withParty(null);

        assertThat(createMessageRequest(message))
            .hasSingleConstraintViolation("messages[0].party", "must not be null");
    }

    @Test
    void shouldFailWithInvalidPartyId() {
        var message = validMessage.withParty(validMessage.party().withPartyId("not-a-uuid"));

        assertThat(createMessageRequest(message))
            .hasSingleConstraintViolation("messages[0].party.partyId", "not a valid UUID");
    }

    @Test
    void shouldFailWithInvalidExternalReference() {
        var externalReference = validMessage.party().externalReferences().get(0);

        // Test invalid external reference key
        var message = validMessage.withParty(validMessage.party()
            .withExternalReferences(List.of(externalReference.withKey(null))));

        assertThat(createMessageRequest(message))
            .hasSingleConstraintViolation("messages[0].party.externalReferences[0].key", "must not be blank");

        // Test invalid external reference value
        message = validMessage.withParty(validMessage.party()
            .withExternalReferences(List.of(externalReference.withValue(null))));

        assertThat(createMessageRequest(message))
            .hasSingleConstraintViolation("messages[0].party.externalReferences[0].value", "must not be blank");
    }

    @Test
    void shouldPassWithNullSender() {
        var message = validMessage.withSender(null);

        assertThat(createMessageRequest(message)).hasNoConstraintViolations();
    }

    @Test
    void shouldFailWithInvalidSender_WhenSenderEmailNameIsNull() {
        var email = validMessage.sender().email().withName(null);
        var sender = validMessage.sender().withEmail(email);
        var message = validMessage.withSender(sender);

        assertThat(createMessageRequest(message))
            .hasSingleConstraintViolation("messages[0].sender.email.name", "must not be blank");
    }

    @Test
    void shouldFailWithInvalidSender_WhenSenderEmailNameIsBlank() {
        var email = validMessage.sender().email().withName("");
        var sender = validMessage.sender().withEmail(email);
        var message = validMessage.withSender(sender);

        assertThat(createMessageRequest(message))
            .hasSingleConstraintViolation("messages[0].sender.email.name", "must not be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"not-a-valid-email-address"})
    void shouldFailWithInvalidSender_WhenSenderEmailIsInvalid(final String address) {
        var email = validMessage.sender().email().withAddress(address);
        var sender = validMessage.sender().withEmail(email);
        var message = validMessage.withSender(sender);

        if (address == null || address.trim().isEmpty()) {
            assertThat(createMessageRequest(message))
                .hasSingleConstraintViolation("messages[0].sender.email.address", "must not be blank");
        } else {
            assertThat(createMessageRequest(message))
                .hasSingleConstraintViolation("messages[0].sender.email.address", "must be a well-formed email address");
        }
    }

    @Test
    void shouldFailWithInvalidSender_WhenSenderEmailReplyToIsInvalid() {
        var email = validMessage.sender().email().withReplyTo("not-an-email-address");
        var sender = validMessage.sender().withEmail(email);
        var message = validMessage.withSender(sender);

        assertThat(createMessageRequest(message))
            .hasSingleConstraintViolation("messages[0].sender.email.replyTo", "must be a well-formed email address");
    }

    @Test
    void shouldFailWithNullMessage() {
        var message = validMessage.withMessage(null);

        assertThat(createMessageRequest(message))
            .hasSingleConstraintViolation("messages[0].message", "must not be blank");
    }

    @Test
    void shouldFailWithBlankMessage() {
        var message = validMessage.withMessage("");

        assertThat(createMessageRequest(message))
            .hasSingleConstraintViolation("messages[0].message", "must not be blank");
    }

    private MessageRequest createMessageRequest(final MessageRequest.Message message) {
        return new MessageRequest(List.of(message));
    }
}
