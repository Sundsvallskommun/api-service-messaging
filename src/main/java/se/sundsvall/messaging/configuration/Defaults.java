package se.sundsvall.messaging.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import se.sundsvall.messaging.api.model.validation.ValidationGroups;
import se.sundsvall.messaging.model.Sender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated(ValidationGroups.Defaults.class)
@ConfigurationProperties(prefix = "messaging.defaults")
public class Defaults {

    @Valid
    @NotNull
    private Sender.Sms sms;

    @Valid
    @NotNull
    private Sender.Email email;

    @Valid
    @NotNull
    private DigitalMail digitalMail;

    @Getter
    @Setter
    public static class DigitalMail extends Sender.DigitalMail {

        @NotBlank
        private String subject;
    }
}
