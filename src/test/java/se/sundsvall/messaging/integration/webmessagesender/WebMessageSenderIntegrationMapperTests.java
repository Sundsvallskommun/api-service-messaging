package se.sundsvall.messaging.integration.webmessagesender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Party;

class WebMessageSenderIntegrationMapperTests {

    private final WebMessageSenderIntegrationMapper mapper = new WebMessageSenderIntegrationMapper();

    @Test
    void test_toCreateWebMessageRequest_whenDtoIsNull() {
        assertThat(mapper.toCreateWebMessageRequest(null)).isNull();
    }

    @Test
    void test_toCreateWebMessageRequest() {
        var dto = WebMessageDto.builder()
            .withParty(Party.builder()
                .withPartyId("somePartyId")
                .withExternalReferences(List.of(
                    ExternalReference.builder()
                        .withKey("key")
                        .withValue("value")
                        .build()
                ))
                .build())
            .withMessage("someMessage")
            .build();

        var request = mapper.toCreateWebMessageRequest(dto);

        assertThat(request.getPartyId()).isEqualTo(dto.getParty().getPartyId());
        assertThat(request.getExternalReferences()).hasSameSizeAs(dto.getParty().getExternalReferences());
        assertThat(request.getMessage()).isEqualTo(dto.getMessage());
    }
}
