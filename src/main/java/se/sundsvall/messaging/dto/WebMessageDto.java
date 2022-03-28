package se.sundsvall.messaging.dto;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(setterPrefix = "with")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebMessageDto {

    private String batchId;
    private String messageId;
    private Party party;
    private String message;
    private MessageStatus status;
}
