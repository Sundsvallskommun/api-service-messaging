package se.sundsvall.messaging.api;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.HistoryResponse;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.service.StatisticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Status and History Resources")
@RestController
class StatusAndHistoryResource {

    static final String BATCH_STATUS_PATH = "/status/batch/{batchId}";
    static final String MESSAGE_STATUS_PATH = "/status/message/{messageId}";
    static final String DELIVERY_STATUS_PATH = "/status/delivery/{deliveryId}";

    private static final Gson GSON = new GsonBuilder().create();

    private final HistoryService historyService;
    private final StatisticsService statisticsService;

    StatusAndHistoryResource(final HistoryService historyService,
            final StatisticsService statisticsService) {
        this.historyService = historyService;
        this.statisticsService = statisticsService;
    }

    @Operation(
        summary = "Get the entire conversation history for a given party",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoryResponse.class)))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(schema = @Schema(implementation = Problem.class))
            )
        }
    )
    @GetMapping(
        value = "/conversation-history/{partyId}",
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<List<HistoryResponse>> getConversationHistory(
            @Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String partyId,

            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "From-date (inclusive). Format: yyyy-MM-dd (ISO8601)")
            final LocalDate from,

            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "To-date (inclusive). Format: yyyy-MM-dd (ISO8601)")
            final LocalDate to) {
        return ResponseEntity.ok(historyService.getConversationHistory(partyId, from, to).stream()
            .map(this::toHistoryResponse)
            .toList());
    }

    @Operation(
        summary = "Get the status for a message batch, its messages and their deliveries",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = MessageBatchResult.class))
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
        }
    )
    @GetMapping(
        value = BATCH_STATUS_PATH,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageBatchResult> getBatchStatus(
            @Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String batchId) {
        var history = historyService.getHistoryByBatchId(batchId);
        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Group the history first by batchId and then by messageId
        var groupedHistory = history.stream()
            .collect(groupingBy(History::batchId, groupingBy(History::messageId)));

        // Sanity check - we should only have a single "root" entry, but just to be safe...
        if (groupedHistory.size() != 1) {
            throw Problem.valueOf(Status.NOT_FOUND, "Unable to get batch status");
        }

        // Grab the first (and only) "root" entry
        var batch = groupedHistory
            .entrySet()
            .iterator()
            .next();

        var result = MessageBatchResult.builder()
            .withBatchId(batch.getKey())
            .withMessages(batch.getValue().entrySet().stream()
                .map(message -> MessageResult.builder()
                    .withMessageId(message.getKey())
                    .withDeliveries(message.getValue().stream()
                        .map(this::toDeliveryResult)
                        .toList())
                    .build())
                .toList())
            .build();

        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Get the status for a single message and its deliveries",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = MessageResult.class))
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
        }
    )
    @GetMapping(
        value = MESSAGE_STATUS_PATH,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageResult> getMessageStatus(
            @Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String messageId) {
        var history = historyService.getHistoryByMessageId(messageId);

        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Group the history by messageId
        var groupedHistory = history.stream().collect(Collectors.groupingBy(History::messageId));

        // Sanity check - we should only have a single "root" entry, but just to be safe...
        if (groupedHistory.size() != 1) {
            throw Problem.valueOf(Status.NOT_FOUND, "Unable to get message status");
        }

        // Grab the first (and only) "root" entry
        var message = groupedHistory
            .entrySet()
            .iterator()
            .next();

        var result = MessageResult.builder()
            .withMessageId(message.getKey())
            .withDeliveries(message.getValue().stream()
                .map(this::toDeliveryResult)
                .toList())
            .build();

        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Get the status for a single delivery",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = DeliveryResult.class))
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
        }
    )
    @GetMapping(
        value = DELIVERY_STATUS_PATH,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<DeliveryResult> getDeliveryStatus(
            @Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String deliveryId) {
        return historyService.getHistoryForDeliveryId(deliveryId)
            .map(this::toDeliveryResult)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Get a message and all its deliveries",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoryResponse.class)))
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
        }
    )
    @GetMapping(
        value = "/message/{messageId}",
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<List<HistoryResponse>> getMessage(
            @Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String messageId) {
        var history = historyService.getHistoryByMessageId(messageId).stream()
            .map(this::toHistoryResponse)
            .toList();

        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(history);
    }

    @Operation(
        summary = "Get delivery statistics",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Statistics.class)))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(schema = @Schema(implementation = Problem.class))
            )
        }
    )
    @GetMapping(
        value = "/statistics",
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<Statistics> getStats(
            @RequestParam(name = "messageType", required = false)
            @Parameter(description = "Message type")
            final MessageType messageType,

            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "From-date (inclusive). Format: yyyy-MM-dd (ISO8601)")
            final LocalDate from,

            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "To-date (inclusive). Format: yyyy-MM-dd (ISO8601)")
            final LocalDate to) {
        var stats = statisticsService.getStatistics(messageType, from, to);

        return ResponseEntity.ok(stats);
    }
    DeliveryResult toDeliveryResult(final History deliveryHistory) {
        return DeliveryResult.builder()
            .withDeliveryId(deliveryHistory.deliveryId())
            .withMessageType(deliveryHistory.messageType())
            .withStatus(deliveryHistory.status())
            .build();
    }

    HistoryResponse toHistoryResponse(final History history) {
        return HistoryResponse.builder()
            .withMessageType(history.messageType())
            .withStatus(history.status())
            .withContent(GSON.fromJson(history.content(), switch (history.messageType()) {
                case EMAIL -> EmailRequest.class;
                case SMS -> SmsRequest.class;
                case WEB_MESSAGE -> WebMessageRequest.class;
                case DIGITAL_MAIL -> DigitalMailRequest.class;
                case MESSAGE -> MessageRequest.Message.class;
                case SNAIL_MAIL -> SnailMailRequest.class;
                case LETTER -> LetterRequest.class;
                case SLACK -> SlackRequest.class;
            }))
            .withTimestamp(history.createdAt())
            .build();
    }
}
