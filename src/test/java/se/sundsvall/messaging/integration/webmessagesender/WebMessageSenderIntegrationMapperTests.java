package se.sundsvall.messaging.integration.webmessagesender;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createExternalReference;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class WebMessageSenderIntegrationMapperTests {

    private final WebMessageSenderIntegrationMapper mapper = new WebMessageSenderIntegrationMapper();

    @Test
    void test_toCreateWebMessageRequest_whenDtoIsNull() {
        assertThat(mapper.toCreateWebMessageRequest(null)).isNull();
    }

    @Test
    void test_toCreateWebMessageRequest() {
        var dto = WebMessageDto.builder()
            .withPartyId("somePartyId")
            .withExternalReferences(List.of(createExternalReference()))
            .withMessage("someMessage")
            .build();

        var mappedRequest = mapper.toCreateWebMessageRequest(dto);

        assertThat(mappedRequest.getPartyId()).isEqualTo(dto.partyId());
        assertThat(mappedRequest.getExternalReferences()).hasSameSizeAs(dto.externalReferences());
        assertThat(mappedRequest.getMessage()).isEqualTo(dto.message());
    }
}
