package se.sundsvall.messaging.configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.model.Sender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "messaging.default-sender")
public class DefaultSettings {

    @Valid
    @NotNull
    private Sender.Sms sms;

    @Valid
    @NotNull
    private Sender.Email email;
}
