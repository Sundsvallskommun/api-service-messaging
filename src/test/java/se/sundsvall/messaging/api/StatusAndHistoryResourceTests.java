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

import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
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
        // TODO: the only reason for this loop is coverage...refactor as a parameterized test ?
        for (var messageType : MessageType.values()) {
            when(mockHistoryService.getConversationHistory(any(String.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(History.builder()
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
        when(mockHistoryService.getHistoryByMessageId(any(String.class)))
            .thenReturn(List.of(
                History.builder()
                    .withBatchId("someBatchId")
                    .withMessageId("someMessageId")
                    .withDeliveryId("someDeliveryId")
                    .withMessageType(MessageType.SMS)
                    .withStatus(MessageStatus.SENT)
                    .build()));

        var response = statusAndHistoryResource.getMessageStatus("someMessageId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(mockHistoryService, times(1)).getHistoryByMessageId(any(String.class));
    }

    @Test
    void test_getMessageStatus_whenMessageDoesNotExist() {
        when(mockHistoryService.getHistoryByMessageId(any(String.class))).thenReturn(List.of());

        var response = statusAndHistoryResource.getMessageStatus("someMessageId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();

        verify(mockHistoryService, times(1)).getHistoryByMessageId(any(String.class));
    }

    @Test
    void test_getBatchStatus() {
        when(mockHistoryService.getHistoryByBatchId(any(String.class)))
            .thenReturn(List.of(
                History.builder()
                    .withBatchId("someBatchId")
                    .withMessageId("someMessageId")
                    .withDeliveryId("someDeliveryId")
                    .withMessageType(MessageType.SMS)
                    .withStatus(MessageStatus.SENT)
                    .build()));

        var response = statusAndHistoryResource.getBatchStatus("someBatchId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messages()).hasSize(1);
    }

    @Test
    void test_getBatchStatus_whenBatchIsEmpty() {
        when(mockHistoryService.getHistoryByBatchId(any(String.class)))
            .thenReturn(List.of());

        var response = statusAndHistoryResource.getBatchStatus("someBatchId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void test_getMessage() {
        when(mockHistoryService.getHistoryByMessageId(any(String.class)))
            .thenReturn(List.of(History.builder().withMessageType(MessageType.SMS).build()));

        var response = statusAndHistoryResource.getMessage("someMessageId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);

        verify(mockHistoryService, times(1)).getHistoryByMessageId(any(String.class));
    }

    @Test
    void test_getMessage_whenMessageDoesNotExist() {
        when(mockHistoryService.getHistoryByMessageId(any(String.class)))
            .thenReturn(List.of());

        var response = statusAndHistoryResource.getMessage("someMessageId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(mockHistoryService, times(1)).getHistoryByMessageId(any(String.class));
    }
}
