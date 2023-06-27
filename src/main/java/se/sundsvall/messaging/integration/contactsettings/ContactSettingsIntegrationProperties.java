package se.sundsvall.messaging.integration.contactsettings;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@ConfigurationProperties(prefix = "integration.contact-settings")
class ContactSettingsIntegrationProperties extends AbstractRestIntegrationProperties {

}
