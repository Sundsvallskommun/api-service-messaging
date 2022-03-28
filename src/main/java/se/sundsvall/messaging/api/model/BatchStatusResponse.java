package se.sundsvall.messaging.api.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BatchStatusResponse {

    private final List<MessageStatusResponse> messageStatuses;
}
