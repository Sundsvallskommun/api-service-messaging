package se.sundsvall.messaging.api;

import java.time.LocalDate;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.messaging.api.model.BatchStatusResponse;
import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.HistoryResponse;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.MessageStatusResponse;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.service.HistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Status and History Resources")
@RestController
class StatusAndHistoryResource {

    private static final Gson GSON = new GsonBuilder().create();

    private final HistoryService historyService;

    StatusAndHistoryResource(final HistoryService historyService) {
        this.historyService = historyService;
    }

    @Operation(summary = "Get the entire conversation history for a given party")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful Operation",
            content = @Content(schema = @Schema(implementation = HistoryResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @GetMapping("/conversationHistory/{partyId}")
    ResponseEntity<List<HistoryResponse>> getConversationHistory(@PathVariable final String partyId,
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Format: yyyy-MM-dd (ISO8601)")
            final LocalDate from,

            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Format: yyyy-MM-dd (ISO8601)")
            final LocalDate to) {
        return ResponseEntity.ok(historyService.getConversationHistory(partyId, from, to).stream()
            .map(historyDto -> HistoryResponse.builder()
                .withMessageType(historyDto.getMessageType())
                .withStatus(historyDto.getStatus())
                .withContent(GSON.fromJson(historyDto.getContent(), switch (historyDto.getMessageType()) {
                    case EMAIL -> EmailRequest.class;
                    case SMS -> SmsRequest.class;
                    case WEB_MESSAGE -> WebMessageRequest.class;
                    case DIGITAL_MAIL -> DigitalMailRequest.class;
                    case MESSAGE -> MessageRequest.Message.class;
                }))
                .withContent(GSON.fromJson(historyDto.getContent(), EmailRequest.class))
                .withTimestamp(historyDto.getCreatedAt())
                .build())
            .toList());
    }

    @Operation(summary = "Get the status for a single message")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful Operation",
            content = @Content(schema = @Schema(implementation = MessageStatusResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(implementation = Problem.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @GetMapping("/status/{messageId}")
    ResponseEntity<MessageStatusResponse> getMessageStatus(@PathVariable final String messageId) {
        return historyService.getHistory(messageId)
            .map(historyDto -> MessageStatusResponse.builder()
                .withMessageId(historyDto.getMessageId())
                .withStatus(historyDto.getStatus())
                .build())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get the status for a message batch")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful Operation",
            content = @Content(schema = @Schema(implementation = BatchStatusResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(schema = @Schema(implementation = Problem.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @GetMapping("/batchStatus/{batchId}")
    ResponseEntity<BatchStatusResponse> getBatchStatus(@PathVariable final String batchId) {
        var messageStatuses = historyService.getHistoryByBatchId(batchId).stream()
            .map(historyDto -> MessageStatusResponse.builder()
                .withMessageId(historyDto.getMessageId())
                .withStatus(historyDto.getStatus())
                .build())
            .toList();

        return ResponseEntity.ok(BatchStatusResponse.builder()
            .withMessageStatuses(messageStatuses)
            .build());
    }
}
