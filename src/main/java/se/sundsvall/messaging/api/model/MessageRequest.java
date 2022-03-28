package se.sundsvall.messaging.api.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import se.sundsvall.messaging.model.Party;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Builder(setterPrefix = "with")
    @Schema(name = "Message", description = "A message to be sent")
    public static class Message {

        @Valid
        @NotNull
        private Party party;

        @Schema(description = "The message subject (for E-mails)")
        private String subject;

        @NotBlank
        @Schema(description = "The message text")
        private String message;

        @Schema(description = "Sender name for E-mail", example = "sender")
        private String emailName;

        @Schema(description = "Sender name for SMS", maxLength = 11, example = "sender")
        @Size(max = 11)
        private String smsName;

        @Email
        @Schema(description = "Sender E-mail address", example = "sender@sender.se")
        private String senderEmail;
    }
}
