package se.sundsvall.messaging.api.response;

import se.sundsvall.messaging.api.MessageStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
public class MessageStatusResponse {

    private final String messageId;
    private final MessageStatus status;
}
