package se.sundsvall.messaging.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.messaging.api.model.BatchStatusResponse;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.HistoryResponse;
import se.sundsvall.messaging.api.model.MessageBatchResponse;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.MessageResponse;
import se.sundsvall.messaging.api.model.MessageStatusResponse;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.dto.MessageBatchDto;
import se.sundsvall.messaging.mapper.MessageMapper;
import se.sundsvall.messaging.model.Party;
import se.sundsvall.messaging.service.EmailService;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.service.SmsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Messaging Resources")
@RestController
@RequestMapping("/messages")
public class MessageResource {

    private final SmsService smsService;
    private final EmailService emailService;
    private final MessageService messageService;
    private final HistoryService historyService;

    public MessageResource(final EmailService emailService, final SmsService smsService,
            final MessageService messageService, final HistoryService historyService) {
        this.emailService = emailService;
        this.messageService = messageService;
        this.smsService = smsService;
        this.historyService = historyService;
    }

    @Operation(summary = "Get status for message by message ID")
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
    @GetMapping("/status/{messageId}")
    // Temporary endpoint to satisfy the endpoint written in the specification
    public ResponseEntity<MessageStatusResponse> getStatus(@PathVariable final String messageId) {
        return getMessageStatus(messageId);
    }

    @Operation(summary = "Get status for message by message ID")
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
    @GetMapping("/{messageId}/status")
    public ResponseEntity<MessageStatusResponse> getMessageStatus(@PathVariable final String messageId) {
        var history = historyService.getHistoryByMessageId(messageId);

        var messageStatusResponse = MessageStatusResponse.builder()
            .withMessageId(history.getMessageId())
            .withStatus(history.getStatus())
            .build();

        return ResponseEntity.ok(messageStatusResponse);
    }

    @Operation(summary = "Get status for messages by batch ID")
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
    @GetMapping("/batch/{batchId}/status")
    public ResponseEntity<BatchStatusResponse> getBatchStatus(@PathVariable final String batchId) {
        var historyEntries = historyService.getHistoryByBatchId(batchId);

        var messageStatusResponses = historyEntries.stream()
            .map(history -> MessageStatusResponse.builder()
                .withMessageId(history.getMessageId())
                .withStatus(history.getStatus())
                .build())
            .toList();

        return ResponseEntity.ok(new BatchStatusResponse(messageStatusResponses));
    }

    @Operation(summary = "Send an SMS")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful Operation",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(schema = @Schema(implementation = Problem.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @PostMapping("/sms")
    public ResponseEntity<MessageResponse> sendSms(@Valid @RequestBody final SmsRequest request) {
        var savedSms = smsService.saveSms(request);

        return ResponseEntity.ok(new MessageResponse(savedSms.getMessageId()));
    }

    @Operation(summary = "Send an E-mail")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful Operation",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(schema = @Schema(implementation = Problem.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @PostMapping("/email")
    public ResponseEntity<MessageResponse> sendEmail(@Valid @RequestBody final EmailRequest request) {
        var savedEmail = emailService.saveEmail(request);

        return ResponseEntity.ok(new MessageResponse(savedEmail.getMessageId()));
    }

    @Operation(summary = "Send messages as e-mail or SMS to a list of parties")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful Operation",
            content = @Content(schema = @Schema(implementation = MessageBatchResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(schema = @Schema(implementation = Problem.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = Problem.class))
        )
    })
    @PostMapping
    public ResponseEntity<MessageBatchResponse> sendMessage(@Valid @RequestBody final MessageRequest request) {
        var messageBatch = messageService.saveIncomingMessages(MessageMapper.toMessageBatch(request));

        var messageIds = messageBatch.getMessages().stream()
            .map(MessageBatchDto.Message::getMessageId)
            .toList();

        return ResponseEntity.ok(MessageBatchResponse.builder()
            .withBatchId(messageBatch.getBatchId())
            .withMessageIds(messageIds)
            .build());
    }

    @Operation(summary = "Get the conversation history for a given party ID")
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
    @GetMapping("/{partyId}/conversationHistory")
    public ResponseEntity<List<HistoryResponse>> getConversationHistory(@PathVariable final String partyId) {
        var result = historyService.getHistoryForPartyId(partyId).stream()
            .map(dto -> HistoryResponse.builder()
                .withMessage(dto.getMessage())
                .withMessageType(dto.getMessageType())
                .withSender(dto.getSender())
                .withParty(Party.builder()
                    .withPartyId(dto.getParty().getPartyId())
                    .withExternalReferences(dto.getParty().getExternalReferences())
                    .build())
                .withStatus(dto.getStatus())
                .withTimestamp(dto.getCreatedAt())
                .build())
            .toList();

        return ResponseEntity.ok(result);
    }
}
