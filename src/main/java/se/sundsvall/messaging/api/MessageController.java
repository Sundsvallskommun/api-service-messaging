package se.sundsvall.messaging.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.messaging.api.request.IncomingEmailRequest;
import se.sundsvall.messaging.api.request.IncomingSmsRequest;
import se.sundsvall.messaging.api.request.MessageRequest;
import se.sundsvall.messaging.api.response.BatchStatusResponse;
import se.sundsvall.messaging.api.response.HistoryResponse;
import se.sundsvall.messaging.api.response.MessageBatchResponse;
import se.sundsvall.messaging.api.response.MessageResponse;
import se.sundsvall.messaging.api.response.MessageStatusResponse;
import se.sundsvall.messaging.mapper.MessageMapper;
import se.sundsvall.messaging.model.dto.EmailDto;
import se.sundsvall.messaging.model.dto.HistoryDto;
import se.sundsvall.messaging.model.dto.MessageBatchDto;
import se.sundsvall.messaging.model.dto.SmsDto;
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

@Tag(name = "Messaging Service Resources")
@RestController
@RequestMapping("/messages")
public class MessageController {

    private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);

    private final SmsService smsService;
    private final EmailService emailService;
    private final MessageService messageService;
    private final HistoryService historyService;

    public MessageController(EmailService emailService,
                             SmsService smsService,
                             MessageService messageService,
                             HistoryService historyService) {
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
    public ResponseEntity<MessageStatusResponse> getStatus(@PathVariable String messageId) {
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
    public ResponseEntity<MessageStatusResponse> getMessageStatus(@PathVariable String messageId) {
        LOG.debug("Requesting status for message: {}", messageId);
        HistoryDto history = historyService.getHistoryByMessageId(messageId);

        MessageStatusResponse messageStatusResponse = MessageStatusResponse.builder()
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
    public ResponseEntity<BatchStatusResponse> getBatchStatus(@PathVariable String batchId) {
        LOG.debug("Requesting status for message batch: {}", batchId);
        List<HistoryDto> historyEntries = historyService.getHistoryByBatchId(batchId);

        List<MessageStatusResponse> messageStatusResponses = historyEntries.stream()
                .map(history -> MessageStatusResponse.builder()
                        .withMessageId(history.getMessageId())
                        .withStatus(history.getStatus())
                        .build())
                .collect(Collectors.toList());

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
    public ResponseEntity<MessageResponse> sendSms(@Valid @RequestBody IncomingSmsRequest smsRequest) {
        LOG.debug("Incoming SMS request: {}", smsRequest.toString());
        SmsDto savedSms = smsService.saveSms(smsRequest);

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
    public ResponseEntity<MessageResponse> sendEmail(@Valid @RequestBody IncomingEmailRequest emailRequest) {
        LOG.debug("Incoming E-mail request: {}", emailRequest.toString());
        EmailDto savedEmail = emailService.saveEmail(emailRequest);

        return ResponseEntity.ok(new MessageResponse(savedEmail.getMessageId()));
    }

    @Operation(summary = "Send messages as E-mail or SMS to a list of parties")
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
    public ResponseEntity<MessageBatchResponse> sendMessage(@Valid @RequestBody MessageRequest messageRequest) {
        LOG.debug("Incoming message batch: {}", messageRequest.toString());
        MessageBatchDto messageBatch = messageService
                .saveIncomingMessages(MessageMapper.toMessageBatch(messageRequest));

        List<String> messageIds = messageBatch.getMessages()
                .stream()
                .map(MessageBatchDto.Message::getMessageId)
                .collect(Collectors.toList());

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
    public ResponseEntity<List<HistoryResponse>> getConversationHistory(@PathVariable String partyId) {
        List<HistoryResponse> result = historyService.getHistoryForPartyId(partyId).stream()
                .map(dto -> HistoryResponse.builder()
                        .withMessage(dto.getMessage())
                        .withMessageType(dto.getMessageType())
                        .withSender(dto.getSender())
                        .withPartyId(dto.getPartyId())
                        .withStatus(dto.getStatus())
                        .withTimestamp(dto.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

}
