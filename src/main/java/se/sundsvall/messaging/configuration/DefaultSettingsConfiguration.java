package se.sundsvall.messaging.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DefaultSettings.class)
class DefaultSettingsConfiguration {

}
