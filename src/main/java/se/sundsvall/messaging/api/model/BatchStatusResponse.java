package se.sundsvall.messaging.api.model;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with", builderClassName = "Builder")
@JsonDeserialize(builder = MessageStatusResponse.Builder.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchStatusResponse {

    private final List<MessageStatusResponse> messageStatuses;
}
