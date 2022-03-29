package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.integration.db.entity.WebMessageEntity;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

class WebMessageMapperTests {

    @Test
    void toEntity_givenWebMessageRequest_returnsNullWhenWebMessageRequestIsNull() {
        assertThat(WebMessageMapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_givenWebMessageRequest_returnsWebMessageEntityWithSameValue() {
        var request = WebMessageRequest.builder()
            .withMessage("someMessage")
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

        var webMessageEntity = WebMessageMapper.toEntity(request);

        assertThat(webMessageEntity.getMessageId()).isNotNull();
        assertThat(webMessageEntity.getBatchId()).isNotNull();
        assertThat(webMessageEntity.getMessage()).isEqualTo(request.getMessage());
        assertThat(webMessageEntity.getPartyId()).isEqualTo(request.getParty().getPartyId());
        assertThat(webMessageEntity.getExternalReferences()).hasSameSizeAs(request.getParty().getExternalReferences());
    }

    @Test
    void toDto_givenWebMessageEntity_returnsNullWhenWebMessageEntityIsNull() {
        assertThat(WebMessageMapper.toDto(null)).isNull();
    }

    @Test
    void toDto_givenWebMessageEntity_returnsWebMessageDtoWithSameValue() {
        var now = LocalDateTime.now();

        var webMessageEntity = WebMessageEntity.builder()
            .withMessageId("someMessageId")
            .withBatchId("someBatchId")
            .withMessage("someMessage")
            .withPartyId("somePartyId")
            .withExternalReferences(Map.of("key", "value"))
            .withStatus(MessageStatus.SENT)
            .build();

        var webMessageDto = WebMessageMapper.toDto(webMessageEntity);

        assertThat(webMessageDto.getMessageId()).isEqualTo(webMessageEntity.getMessageId());
        assertThat(webMessageDto.getBatchId()).isEqualTo(webMessageEntity.getBatchId());
        assertThat(webMessageDto.getMessage()).isEqualTo(webMessageEntity.getMessage());
        assertThat(webMessageDto.getParty().getPartyId()).isEqualTo(webMessageEntity.getPartyId());
        assertThat(webMessageDto.getParty().getExternalReferences()).hasSameSizeAs(webMessageEntity.getExternalReferences().entrySet());
        assertThat(webMessageDto.getStatus()).isEqualTo(webMessageEntity.getStatus());
    }
}
