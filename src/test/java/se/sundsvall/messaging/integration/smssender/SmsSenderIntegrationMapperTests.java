package se.sundsvall.messaging.integration.smssender;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.dto.SmsDto;

class SmsSenderIntegrationMapperTests {

    private final SmsSenderIntegrationMapper mapper = new SmsSenderIntegrationMapper();

    @Test
    void toRequest_givenNullRequest_returnsNull() {
        assertThat(mapper.toRequest(null)).isNull();
    }

    @Test
    void toRequest_givenSmsDto_returnsSmsRequestWithSameValues() {
        var smsDto = SmsDto.builder()
            .withSender("test sender")
            .withMessage("test")
            .withMobileNumber("+46701234567")
            .build();

        var request = mapper.toRequest(smsDto);

        assertThat(request.getMessage()).isEqualTo(smsDto.getMessage());
        assertThat(request.getSender()).isEqualTo(smsDto.getSender());
        assertThat(request.getMobileNumber()).isEqualTo(smsDto.getMobileNumber());
    }
}
