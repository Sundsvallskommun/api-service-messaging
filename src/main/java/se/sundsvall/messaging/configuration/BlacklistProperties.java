package se.sundsvall.messaging.configuration;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import se.sundsvall.messaging.model.MessageType;

@ConfigurationProperties(prefix = "messaging.blacklist")
public record BlacklistProperties(
    boolean enabled,
    Map<MessageType, List<String>> blockedRecipients) {

    @ConstructorBinding
    public BlacklistProperties { }
}
