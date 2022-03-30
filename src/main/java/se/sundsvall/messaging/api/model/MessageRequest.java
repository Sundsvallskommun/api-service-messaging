package se.sundsvall.messaging.api.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import se.sundsvall.messaging.model.Sender;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
@Schema(name = "MessageRequest", description = "Message representation")
public class MessageRequest {

    @Schema(description = "The messages to be sent")
    @Valid
    private List<Message> messages;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @SuperBuilder(setterPrefix = "with")
    @Schema(name = "Message", description = "A message to be sent")
    public static class Message extends Request {

        @Valid
        @Schema(description = "Sender")
        private Sender sender;

        @Schema(description = "The message subject (for E-mails)")
        private String subject;

        @NotBlank
        @Schema(description = "The message text")
        private String message;
    }
}
