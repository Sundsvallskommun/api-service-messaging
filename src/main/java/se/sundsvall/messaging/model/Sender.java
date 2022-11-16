package se.sundsvall.messaging.model;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @Valid
    private Sender.DigitalMail digitalMail;

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

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    public static class DigitalMail {

        @NotBlank
        @JsonIgnore // Since this shouldn't be possible to set in requests
        private String municipalityId;

        @Valid
        @NotNull
        private SupportInfo supportInfo;

        @Getter
        @Setter
        @EqualsAndHashCode
        @NoArgsConstructor
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        @Builder(setterPrefix = "with")
        public static class SupportInfo {

            @NotBlank
            private String text;

            @javax.validation.constraints.Email
            @NotBlank
            private String emailAddress;

            @NotBlank
            private String phoneNumber;

            @NotBlank
            private String url;
        }
    }
}
