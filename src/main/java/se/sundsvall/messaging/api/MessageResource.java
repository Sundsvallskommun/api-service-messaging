package se.sundsvall.messaging.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.LetterRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.MessageResponse;
import se.sundsvall.messaging.api.model.MessagesResponse;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.SnailmailRequest;
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
    @PostMapping(
        value = "/sms",
        consumes = APPLICATION_JSON_VALUE,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageResponse> sendSms(@Valid @RequestBody final SmsRequest request) {
        var message = messageService.handleSmsRequest(request);

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
    @PostMapping(
        value = "/webmessage",
        consumes = APPLICATION_JSON_VALUE,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageResponse> sendWebMessage(@Valid @RequestBody final WebMessageRequest request) {
        var message = messageService.handleWebMessageRequest(request);

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
    @PostMapping(
        value = "/email",
        consumes = APPLICATION_JSON_VALUE,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageResponse> sendEmail(@Valid @RequestBody final EmailRequest request) {
        var message = messageService.handleEmailRequest(request);

        return ResponseEntity.ok(new MessageResponse(message.getMessageId()));
    }

    @Operation(summary = "Send a single digital mail to one or more parties")
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
    @PostMapping(
        value = "/digitalmail",
        consumes = APPLICATION_JSON_VALUE,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessagesResponse> sendDigitalMail(@Valid @RequestBody final DigitalMailRequest request) {
        var messages = messageService.handleDigitalMailRequest(request);

        return ResponseEntity.ok(MessagesResponse.builder()
            .withBatchId(messages.getBatchId())
            .withMessageIds(messages.getMessageIds())
            .build());
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
    @PostMapping(
        value = "/messages",
        consumes = APPLICATION_JSON_VALUE,
            produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE}
    )
    ResponseEntity<MessagesResponse> sendMessages(@Valid @RequestBody final MessageRequest request) {
        var messages = messageService.handleMessageRequest(request);

        return ResponseEntity.ok(MessagesResponse.builder()
                .withBatchId(messages.getBatchId())
                .withMessageIds(messages.getMessageIds())
                .build());
    }


    @Operation(summary = "Send a single snailmail")
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
    @PostMapping(
            value = "/snailmail",
            consumes = APPLICATION_JSON_VALUE,
            produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE}
    )
    ResponseEntity<MessageResponse> sendSnailmail(@Valid @RequestBody final SnailmailRequest request) {
        var message = messageService.handleSnailmailRequest(request);

        return ResponseEntity.ok(new MessageResponse(message.getMessageId()));
    }

    @Operation(summary = "Send a single letter as digital mail or snail mail")
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
    @PostMapping(
            value = "/letter",
            consumes = APPLICATION_JSON_VALUE,
            produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE}
    )
    ResponseEntity<MessagesResponse> sendLetter(@Valid @RequestBody final LetterRequest request) {
        var messages = messageService.handleLetterRequest(request);

        return ResponseEntity.ok(MessagesResponse.builder()
                .withBatchId(messages.getBatchId())
                .withMessageIds(messages.getMessageIds())
                .build());
    }
}
