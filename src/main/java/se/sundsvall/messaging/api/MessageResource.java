package se.sundsvall.messaging.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.MessageResponse;
import se.sundsvall.messaging.api.model.MessagesResponse;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Sending Resources")
@RestController
@RequestMapping("/send")
class MessageResource {

    private final MessageService messageService;

    MessageResource(final MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "Send a single SMS")
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
    ResponseEntity<MessageResponse> sendSms(@Valid @RequestBody final SmsRequest request) {
        var message = messageService.saveSmsRequest(request);

        return ResponseEntity.ok(new MessageResponse(message.getMessageId()));
    }

    @Operation(summary = "Send a single web message")
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
    @PostMapping("/webmessage")
    ResponseEntity<MessageResponse> sendWebMessage(@Valid @RequestBody final WebMessageRequest request) {
        var message = messageService.saveWebMessageRequest(request);

        return ResponseEntity.ok(new MessageResponse(message.getMessageId()));
    }

    @Operation(summary = "Send a single e-mail")
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
    ResponseEntity<MessageResponse> sendEmail(@Valid @RequestBody final EmailRequest request) {
        var message = messageService.saveEmailRequest(request);

        return ResponseEntity.ok(new MessageResponse(message.getMessageId()));
    }

    @Operation(summary = "Send a batch of messages as e-mail or SMS to a list of parties")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successful Operation",
            content = @Content(schema = @Schema(implementation = MessagesResponse.class))
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
    @PostMapping("/batch")
    ResponseEntity<MessagesResponse> sendMessage(@Valid @RequestBody final MessageRequest request) {
        var messages = messageService.saveMessageRequest(request);

        return ResponseEntity.ok(MessagesResponse.builder()
            .withBatchId(messages.getBatchId())
            .withMessageIds(messages.getMessageIds())
            .build());
    }
}
