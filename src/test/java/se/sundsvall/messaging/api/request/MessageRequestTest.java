package se.sundsvall.messaging.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MessageRequestTest {

    @Test
    void testBuilderAndGetters() {
        String partyId = UUID.randomUUID().toString();

        MessageRequest.Message message = MessageRequest.Message.builder()
                .withPartyId(partyId)
                .withMessage("message")
                .withSubject("subject")
                .withSenderEmail("noreply@sundsvall.se")
                .withSmsName("Sundsvall")
                .withEmailName("Sundsvalls kommun")
                .build();

        MessageRequest messageRequest = MessageRequest.builder()
                .withMessages(List.of(message))
                .build();

        // Just for coverage
        assertThat(messageRequest.toString()).isNotNull();
        assertThat(messageRequest.getMessages()).hasSize(1)
                .allSatisfy(msg -> {
                    assertThat(msg.getPartyId()).isEqualTo(partyId);
                    assertThat(msg.getMessage()).isEqualTo("message");
                    assertThat(msg.getSubject()).isEqualTo("subject");
                    assertThat(msg.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
                    assertThat(msg.getSmsName()).isEqualTo("Sundsvall");
                    assertThat(msg.getEmailName()).isEqualTo("Sundsvalls kommun");
                    // Just for coverage
                    assertThat(msg.toString()).isNotNull();
                });
    }

    @Test
    void testSetters() {
        String partyId = UUID.randomUUID().toString();

        MessageRequest.Message message = new MessageRequest.Message();
        message.setPartyId(partyId);
        message.setMessage("message");
        message.setSubject("subject");
        message.setSenderEmail("noreply@sundsvall.se");
        message.setSmsName("Sundsvall");
        message.setEmailName("Sundsvalls kommun");

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setMessages(List.of(message));

        // Just for coverage
        assertThat(messageRequest.toString()).isNotNull();
        assertThat(messageRequest.getMessages()).hasSize(1)
                .allSatisfy(msg -> {
                    assertThat(msg.getPartyId()).isEqualTo(partyId);
                    assertThat(msg.getMessage()).isEqualTo("message");
                    assertThat(msg.getSubject()).isEqualTo("subject");
                    assertThat(msg.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
                    assertThat(msg.getSmsName()).isEqualTo("Sundsvall");
                    assertThat(msg.getEmailName()).isEqualTo("Sundsvalls kommun");
                    // Just for coverage
                    assertThat(msg.toString()).isNotNull();
                });
    }
}
