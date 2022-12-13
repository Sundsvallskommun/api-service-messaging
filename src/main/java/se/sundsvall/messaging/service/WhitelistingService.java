package se.sundsvall.messaging.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.integration.db.AllowedRecipientRepository;
import se.sundsvall.messaging.model.MessageType;

@Service
public class WhitelistingService {

    private static final Logger LOG = LoggerFactory.getLogger(WhitelistingService.class);

    private final boolean enabled;
    private final AllowedRecipientRepository allowedRecipientRepository;

    public WhitelistingService(@Value("${whitelisting.enabled:false}") final boolean enabled,
            final AllowedRecipientRepository allowedRecipientRepository) {
        this.enabled = enabled;
        this.allowedRecipientRepository = allowedRecipientRepository;

        LOG.info("Whitelisting service is {}", enabled ? "ENABLED" : "DISABLED");
    }

    public boolean isWhitelisted(final MessageType messageType, final String recipient) {
        if (!enabled) {
            return true;
        }

        return allowedRecipientRepository.findByMessageTypeAndRecipient(messageType, recipient).isPresent();
    }
}
