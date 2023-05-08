package se.sundsvall.messaging.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "messaging.defaults")
public record Defaults(Sms sms, Email email, DigitalMail digitalMail) {

    @ConstructorBinding
    public Defaults(
            @Valid @NotNull final Sms sms,
            @Valid @NotNull final Email email,
            @Valid @NotNull final DigitalMail digitalMail) {
        this.sms = sms;
        this.email = email;
        this.digitalMail = digitalMail;
    }

    public record Sms(

        @NotBlank
        @Size(max = 11)
        String name) { }

    public record Email(

        @NotBlank
        String name,

        @NotBlank
        @jakarta.validation.constraints.Email
        String address,

        @jakarta.validation.constraints.Email
        String replyTo) { }

    public record DigitalMail(

            @NotBlank
            @JsonIgnore // Since this shouldn't be possible to set in requests
            String municipalityId,

            @Valid
            @NotNull
            SupportInfo supportInfo,

            String subject) {

        public record SupportInfo(

            @NotBlank
            String text,

            @jakarta.validation.constraints.Email
            @NotBlank
            String emailAddress,

            @NotBlank
            String phoneNumber,

            @NotBlank
            String url) { }
    }
}
