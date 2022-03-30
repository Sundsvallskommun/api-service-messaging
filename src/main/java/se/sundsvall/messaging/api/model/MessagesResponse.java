package se.sundsvall.messaging.api.model;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessagesResponse {

    private final String batchId;
    private final List<String> messageIds;
}
