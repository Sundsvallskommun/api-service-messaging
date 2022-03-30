package se.sundsvall.messaging.model;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
public class Sender {

    @Valid
    private Sender.Sms sms;

    @Valid
    private Sender.Email email;

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    public static class Sms {

        @NotBlank
        @Size(max = 11)
        @Schema(description = "The sender of the SMS", maxLength = 11, example = "sender")
        private String name;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    public static class Email {

        @NotBlank
        @Schema(description = "The sender of the e-mail")
        private String name;

        @NotBlank
        @Schema(description = "Sender e-mail address", example = "sender@sender.se")
        @javax.validation.constraints.Email
        private String address;

        @Schema(description = "Reply-to e-mail address", example = "sender@sender.se")
        @javax.validation.constraints.Email
        private String replyTo;
    }
}
