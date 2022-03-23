package se.sundsvall.messaging.api.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
@Schema(name = "MessageRequest", description = "Message representation")
@ToString
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
    @ToString
    public static class Message {

        @NotBlank
        @Schema(description = "The message party ID", example = "f427952b-247c-4d3b-b081-675a467b3619")
        private String partyId;

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
