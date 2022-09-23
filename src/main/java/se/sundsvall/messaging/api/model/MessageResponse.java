package se.sundsvall.messaging.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with", builderClassName = "Builder")
@JsonDeserialize(builder = MessageResponse.Builder.class) // FOR TESTS
@AllArgsConstructor
public class MessageResponse {

    private final String messageId;
}

