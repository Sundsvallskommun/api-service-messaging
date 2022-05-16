package se.sundsvall.messaging.integration.digitalmailsender;

import java.time.Duration;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.messaging.integration.AbstractRestIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.digital-mail-sender")
public class DigitalMailSenderIntegrationProperties extends AbstractRestIntegrationProperties {

    private Duration pollDelay = Duration.ofSeconds(5);
    private int maxRetries = 3;

    @Valid
    @NotNull
    private Defaults defaults;

    @Getter
    @Setter
    public static class Defaults {

        @NotBlank
        private String municipalityId;

        @NotBlank
        private String subject;

        @Valid
        @NotNull
        private SupportInfo supportInfo;

        @Getter
        @Setter
        public static class SupportInfo {

            @NotBlank
            private String text;

            @Email
            @NotBlank
            private String emailAddress;

            @NotBlank
            private String phoneNumber;

            @NotBlank
            private String url;
        }

    }
}
