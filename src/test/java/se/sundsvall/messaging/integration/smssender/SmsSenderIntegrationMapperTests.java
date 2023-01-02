package se.sundsvall.messaging.integration.smssender;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SmsSenderIntegrationMapperTests {

    private final SmsSenderIntegrationMapper mapper = new SmsSenderIntegrationMapper();

    @Test
    void test_toSendSmsRequest_whenSmsRequestIsNull() {
        assertThat(mapper.toSendSmsRequest(null)).isNull();
    }

    @Test
    void test_toSendSmsRequest() {
        var dto = SmsDto.builder()
            .withSender("someName")
            .withMobileNumber("someMobileNumber")
            .withMessage("someMessage")
            .build();

        var sendSmsRequest = mapper.toSendSmsRequest(dto);

        assertThat(sendSmsRequest.getSender().getName()).isEqualTo("someName");
        assertThat(sendSmsRequest.getMobileNumber()).isEqualTo("someMobileNumber");
        assertThat(sendSmsRequest.getMessage()).isEqualTo("someMessage");
    }
}
