package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class StatusAndHistoryResourceTests {

    @Mock
    private HistoryService mockHistoryService;

    @InjectMocks
    private StatusAndHistoryResource statusAndHistoryResource;

    @ParameterizedTest
    @EnumSource(MessageType.class)
    void test_getConversationHistory(final MessageType messageType) {
        when(mockHistoryService.getConversationHistory(any(String.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(History.builder()
                .withMessageType(messageType)
                .build()));

        var response = statusAndHistoryResource.getConversationHistory(
            "somePartyId", LocalDate.now(), LocalDate.now());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);

        verify(mockHistoryService, times(1))
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
                    .withMessageType(SMS)
                    .withStatus(SENT)
                    .build()));

        var response = statusAndHistoryResource.getMessageStatus("someMessageId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();

        verify(mockHistoryService, times(1)).getHistoryByMessageId(any(String.class));
    }

    @Test
    void test_getMessageStatus_whenMessageDoesNotExist() {
        when(mockHistoryService.getHistoryByMessageId(any(String.class))).thenReturn(List.of());

        var response = statusAndHistoryResource.getMessageStatus("someMessageId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
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
                    .withMessageType(SMS)
                    .withStatus(SENT)
                    .build()));

        var response = statusAndHistoryResource.getBatchStatus("someBatchId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messages()).hasSize(1);
    }

    @Test
    void test_getBatchStatus_whenBatchIsEmpty() {
        when(mockHistoryService.getHistoryByBatchId(any(String.class)))
            .thenReturn(List.of());

        var response = statusAndHistoryResource.getBatchStatus("someBatchId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    void test_getMessage() {
        when(mockHistoryService.getHistoryByMessageId(any(String.class)))
            .thenReturn(List.of(History.builder().withMessageType(SMS).build()));

        var response = statusAndHistoryResource.getMessage("someMessageId");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(OK);
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
        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);

        verify(mockHistoryService, times(1)).getHistoryByMessageId(any(String.class));
    }

    @Test
    void test_toDeliveryResult() {
        var history = History.builder()
            .withDeliveryId("someDeliveryId")
            .withMessageType(WEB_MESSAGE)
            .withStatus(FAILED)
            .build();

        var result = statusAndHistoryResource.toDeliveryResult(history);

        assertThat(result.deliveryId()).isEqualTo(history.deliveryId());
        assertThat(result.messageType()).isEqualTo(history.messageType());
        assertThat(result.status()).isEqualTo(history.status());
    }

    @ParameterizedTest
    @EnumSource(MessageType.class)
    void test_toHistoryResponse(final MessageType messageType) {
        var history = History.builder()
            .withMessageType(messageType)
            .withStatus(SENT)
            .withContent("{}")
            .withCreatedAt(LocalDateTime.now())
            .build();

        var result = statusAndHistoryResource.toHistoryResponse(history);

        assertThat(result).isNotNull();
        assertThat(result.messageType()).isEqualTo(messageType);
        assertThat(result.content()).isNotNull();
        assertThat(result.status()).isEqualTo(SENT);
        assertThat(result.timestamp()).isNotNull();
    }
}
