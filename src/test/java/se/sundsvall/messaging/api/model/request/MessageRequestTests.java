package se.sundsvall.messaging.api.model.request;

import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class MessageRequestTests {

    private static final String PARTY_ID = "somePartyId";
    private static final String SENDER_NAME = "someSender";
    private static final String SENDER_EMAIL = "someone@something.com";
    private static final String FILTER_KEY = "filterKey";
    private static final String SUBJECT = "someSubject";
    private static final String MESSAGE = "someMessage";
    private static final String HTML_MESSAGE = "someHtmlMessage";

    @Test
    void testConstructorAndGetters() {
        var party = new MessageRequest.Message.Party(PARTY_ID, List.of());
        var sender = new MessageRequest.Message.Sender(
            new MessageRequest.Message.Sender.Email(SENDER_NAME, SENDER_EMAIL, SENDER_EMAIL),
            new MessageRequest.Message.Sender.Sms(SENDER_NAME)
        );
        var filters = new LinkedMultiValueMap<>(Map.of(FILTER_KEY, List.<String>of()));
        var message = new MessageRequest.Message(party, filters, sender, SUBJECT, MESSAGE, HTML_MESSAGE);
        var request = new MessageRequest("someOrigin", List.of(message));

        assertThat(request.origin()).isEqualTo("someOrigin");
        assertThat(request.messages()).hasSize(1);
        assertThat(request.messages().get(0)).satisfies(requestMessage -> {
            assertThat(requestMessage.party()).isNotNull().satisfies(requestParty -> {
                assertThat(requestParty.partyId()).isEqualTo(PARTY_ID);
                assertThat(requestParty.externalReferences()).isNotNull().isEmpty();
            });
            assertThat(requestMessage.sender()).isNotNull().satisfies(requestSender -> {
                assertThat(requestSender.email()).isNotNull().satisfies(requestSenderEmail -> {
                    assertThat(requestSenderEmail.name()).isEqualTo(SENDER_NAME);
                    assertThat(requestSenderEmail.address()).isEqualTo(SENDER_EMAIL);
                    assertThat(requestSenderEmail.replyTo()).isEqualTo(SENDER_EMAIL);
                });
                assertThat(requestSender.sms()).isNotNull();
                assertThat(requestSender.sms().name()).isEqualTo(SENDER_NAME);
            });
            assertThat(requestMessage.subject()).isEqualTo(SUBJECT);
            assertThat(requestMessage.message()).isEqualTo(MESSAGE);
            assertThat(requestMessage.htmlMessage()).isEqualTo(HTML_MESSAGE);
            assertThat(requestMessage.filters()).isNotNull().hasSize(1).containsOnlyKeys(FILTER_KEY);
        });
    }
}
