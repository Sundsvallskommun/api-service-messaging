package se.sundsvall.messaging.integration.smssender;

import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import java.util.Optional;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.model.MessageStatus;

import generated.se.sundsvall.smssender.SendSmsResponse;

@Component
@EnableConfigurationProperties(SmsSenderIntegrationProperties.class)
public class SmsSenderIntegration {

    static final String INTEGRATION_NAME = "SmsSender";

    private final SmsSenderClient client;
    private final SmsSenderIntegrationMapper mapper;

    public SmsSenderIntegration(final SmsSenderClient client, final SmsSenderIntegrationMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public MessageStatus sendSms(final SmsDto dto) {
        var response = client.sendSms(mapper.toSendSmsRequest(dto));

        var success = response.getStatusCode().is2xxSuccessful() &&
            Optional.ofNullable(response.getBody())
                .map(SendSmsResponse::getSent)
                .orElse(false);

        return success ? SENT : NOT_SENT;
    }
}
