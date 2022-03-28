package se.sundsvall.messaging.integration.webmessagesender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

class WebMessageSenderIntegrationMapperTests {

    private final WebMessageSenderIntegrationMapper mapper = new WebMessageSenderIntegrationMapper();

    @Test
    void toCreateWebMessageRequest_givenNullRequest_returnsNull() {
        assertThat(mapper.toCreateWebMessageRequest(null)).isNull();
    }

    @Test
    void toCreateWebMessageRequest_givenWebMessageDto_returnsCreteWebMessageRequestWithSameValues() {
        var webMessageDto = WebMessageDto.builder()
            .withMessage("someMessage")
            .withStatus(MessageStatus.PENDING)
            .withParty(Party.builder()
                .withPartyId("somePartyId")
                .withExternalReferences(List.of(
                    ExternalReference.builder()
                        .withKey("key")
                        .withValue("value")
                        .build()
                ))
                .build())
            .build();

        var request = mapper.toCreateWebMessageRequest(webMessageDto);

        assertThat(request.getMessage()).isEqualTo(webMessageDto.getMessage());
        assertThat(request.getPartyId()).isEqualTo("somePartyId");
        assertThat(request.getExternalReferences()).hasSameSizeAs(webMessageDto.getParty().getExternalReferences());
    }
}
