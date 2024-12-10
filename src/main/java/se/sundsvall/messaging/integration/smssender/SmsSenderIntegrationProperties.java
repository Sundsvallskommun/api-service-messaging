package se.sundsvall.messaging.integration.smssender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.sms-sender")
class SmsSenderIntegrationProperties extends AbstractRestIntegrationProperties {

}
