package se.sundsvall.messaging.dto;

import java.util.List;

import se.sundsvall.messaging.model.Party;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageBatchDto {

    private final String batchId;
    private final List<Message> messages;

    @Getter
    @Builder(setterPrefix = "with", toBuilder = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Message {

        private final String messageId;
        private final Party party;
        private final String smsName;
        private final String emailName;
        private final String senderEmail;
        private final String subject;
        private final String message;
    }
}
