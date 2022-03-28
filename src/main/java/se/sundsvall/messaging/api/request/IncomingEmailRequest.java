package se.sundsvall.messaging.api.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import se.sundsvall.messaging.model.Party;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
@Getter
@Setter
@Schema(name = "IncomingEmailRequest", description = "E-mail representation")
public class IncomingEmailRequest {

    @Schema(description = "Recipient e-mail address", example = "recipient@recipient.se" )
    @NotBlank
    private String emailAddress;

    @Valid
    @NotNull
    private Party party;

    @Schema(description = "E-mail subject")
    @NotBlank
    private String subject;

    @Schema(description = "E-mail plain-text body")
    private String message;

    @Schema(description = "E-mail HTML body (BASE64-encoded)")
    private String htmlMessage;

    @Schema(description = "Sender name")
    private String senderName;

    @Schema(description = "Sender e-mail address", example = "sender@sender.se")
    @Email
    private String senderEmail;

    @Schema(description = "Attachments")
    @Valid
    private List<Attachment> attachments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    public static class Attachment {

        @Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK")
        @NotBlank
        private String content;

        @Schema(description = "The attachment filename", example = "test.txt")
        @NotBlank
        private String name;

        @Schema(description = "The attachment content type", example = "text/plain")
        @NotBlank
        private String contentType;
    }
}
