package se.sundsvall.messaging.integration.smssender;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.model.Sender;

class SmsSenderIntegrationMapperTests {

    private final SmsSenderIntegrationMapper mapper = new SmsSenderIntegrationMapper();

    @Test
    void test_toSendSmsRequest_whenDtoIsNull() {
        assertThat(mapper.toSendSmsRequest(null)).isNull();
    }

    @Test
    void test_toSendSmsRequest() {
        var dto = SmsDto.builder()
            .withSender(Sender.Sms.builder()
                .withName("someName")
                .build())
            .withMobileNumber("someMobileNumber")
            .withMessage("someMessage")
            .build();

        var request = mapper.toSendSmsRequest(dto);

        assertThat(request.getSender().getName()).isEqualTo("someName");
        assertThat(request.getMobileNumber()).isEqualTo("someMobileNumber");
        assertThat(request.getMessage()).isEqualTo("someMessage");
    }
}
