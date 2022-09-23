package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import se.sundsvall.messaging.dto.HistoryDto;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.HistoryService;

@ExtendWith(MockitoExtension.class)
class StatusAndHistoryResourceTests {

    @Mock
    private HistoryService mockHistoryService;

    private StatusAndHistoryResource statusAndHistoryResource;

    @BeforeEach
    void setUp() {
        statusAndHistoryResource = new StatusAndHistoryResource(mockHistoryService);
    }

    @Test
    void test_getConversationHistory() {
        // The only reason for this Loop is coverage...
        for (var messageType : MessageType.values()) {
            when(mockHistoryService.getConversationHistory(any(String.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(HistoryDto.builder()
                    .withMessageType(messageType)
                    .build()));

            var response = statusAndHistoryResource.getConversationHistory(
                "somePartyId", LocalDate.now(), LocalDate.now());

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
        }

        verify(mockHistoryService, times(MessageType.values().length))
            .getConversationHistory(any(String.class), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void test_getMessageStatus() {
        when(mockHistoryService.getHistory(any(String.class)))
            .thenReturn(List.of(HistoryDto.builder().build()));

        var response = statusAndHistoryResource.getMessageStatus("someMessageId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(mockHistoryService, times(1)).getHistory(any(String.class));
    }

    @Test
    void test_getMessageStatus_whenMessageDoesNotExist() {
        when(mockHistoryService.getHistory(any(String.class))).thenReturn(List.of());

        var response = statusAndHistoryResource.getMessageStatus("someMessageId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(mockHistoryService, times(1)).getHistory(any(String.class));
    }

    @Test
    void test_getBatchStatus() {
        when(mockHistoryService.getHistoryByBatchId(any(String.class)))
            .thenReturn(List.of(HistoryDto.builder().build()));

        var response = statusAndHistoryResource.getBatchStatus("someBatchId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessages()).hasSize(1);
    }
}
