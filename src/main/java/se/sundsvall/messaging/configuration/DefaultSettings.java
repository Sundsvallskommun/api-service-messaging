package se.sundsvall.messaging.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "messaging.default-sender")
public class DefaultSettings {

    private String smsName;
    private String emailName;
    private String emailAddress;
}
