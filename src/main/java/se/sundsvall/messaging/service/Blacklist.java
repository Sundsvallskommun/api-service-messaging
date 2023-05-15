package se.sundsvall.messaging.service;

import static org.apache.commons.collections4.MapUtils.isEmpty;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.messaging.configuration.BlacklistProperties;
import se.sundsvall.messaging.model.MessageType;

@Service
public class Blacklist {

    private static final Logger LOG = LoggerFactory.getLogger(Blacklist.class);

    private final BlacklistProperties properties;

    public Blacklist(final BlacklistProperties properties) {
        this.properties = properties;

        if (properties.enabled()) {
            LOG.info("Blacklist is ENABLED");
        } else {
            LOG.info("Blacklist is NOT ENABLED");
        }
    }

    public void check(final MessageType messageType, final String value) {
        if (!properties.enabled() || isEmpty(properties.blockedRecipients())) {
            LOG.info("Blacklist is NOT ENABLED, or no blocked recipients have been defined");

            return;
        }

        if (properties.blockedRecipients().getOrDefault(messageType, List.of()).contains(value)) {
            throw Problem.valueOf(Status.BAD_REQUEST, "%s is blacklisted for %s".formatted(value, messageType));
        }
    }
}
