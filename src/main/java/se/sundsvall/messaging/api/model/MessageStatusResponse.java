package se.sundsvall.messaging.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import se.sundsvall.messaging.model.MessageStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with", builderClassName = "Builder")
@JsonDeserialize(builder = MessageStatusResponse.Builder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageStatusResponse {

    private final String messageId;
    private final MessageStatus status;
}
