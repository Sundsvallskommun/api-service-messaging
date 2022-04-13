package se.sundsvall.messaging.dto;

import se.sundsvall.messaging.model.Party;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebMessageDto {

    private Party party;
    private String message;
}
