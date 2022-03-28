package se.sundsvall.messaging.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultSettingsTests {

    private DefaultSettings defaultSettings;

    @BeforeEach
    void setUp() {
        defaultSettings = new DefaultSettings();
        defaultSettings.setSmsName("Sundsvall");
        defaultSettings.setEmailName("Sundsvalls kommun");
        defaultSettings.setEmailAddress("noreply@sundsvall.se");
    }

    @Test
    void propertiesAreLoaded() {
        assertThat(defaultSettings.getSmsName()).isEqualTo("Sundsvall");
        assertThat(defaultSettings.getEmailName()).isEqualTo("Sundsvalls kommun");
        assertThat(defaultSettings.getEmailAddress()).isEqualTo("noreply@sundsvall.se");
    }
}
