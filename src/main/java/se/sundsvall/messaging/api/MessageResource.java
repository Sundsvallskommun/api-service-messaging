package se.sundsvall.messaging.api;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static se.sundsvall.messaging.api.StatusAndHistoryResource.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.api.StatusAndHistoryResource.MESSAGE_STATUS_PATH;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Sending Resources")
@RestController
@ApiResponse(
    responseCode = "201",
    description = "Successful Operation",
    content = @Content(schema = @Schema(implementation = MessageResult.class)),
    headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(type = "string"))
)
@ApiResponse(
    responseCode = "400",
    description = "Bad Request",
    content = @Content(schema = @Schema(implementation = Problem.class))
)
@ApiResponse(
    responseCode = "500",
    description = "Internal Server Error",
    content = @Content(schema = @Schema(implementation = Problem.class))
)
@ApiResponse(
    responseCode = "502",
    description = "Bad Gateway",
    content = @Content(schema = @Schema(implementation = Problem.class))
)
class MessageResource {

    private final MessageService messageService;
    private final MessageEventDispatcher eventDispatcher;

    MessageResource(final MessageService messageService, final MessageEventDispatcher eventDispatcher) {
        this.messageService = messageService;
        this.eventDispatcher = eventDispatcher;
    }

    @Operation(summary = "Send a single SMS")
    @PostMapping(
        value = "/sms",
        consumes = APPLICATION_JSON_VALUE,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageResult> sendSms(@Valid @RequestBody final SmsRequest request,
            @Parameter(description = "Whether to send the message asynchronously")
            @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async,
            final UriComponentsBuilder uriComponentsBuilder) {
        if (async) {
            return toResponse(uriComponentsBuilder, eventDispatcher.handleSmsRequest(request));
        } else {
            return toResponse(uriComponentsBuilder, messageService.sendSms(request));
        }
    }

    @Operation(
        summary = "Send a single e-mail",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = MessageResult.class)),
                headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(type = "string"))
            )
        }
    )
    @PostMapping(
        value = "/email",
        consumes = APPLICATION_JSON_VALUE,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageResult> sendEmail(@Valid @RequestBody final EmailRequest request,
            @Parameter(description = "Whether to send the message asynchronously")
            @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async,
            final UriComponentsBuilder uriComponentsBuilder) {
        if (async) {
            return toResponse(uriComponentsBuilder, eventDispatcher.handleEmailRequest(request));
        } else {
            return toResponse(uriComponentsBuilder, messageService.sendEmail(request));
        }
    }

    @Operation(
        summary = "Send a single web message",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = MessageResult.class)),
                headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(type = "string"))
            )
        }
    )
    @PostMapping(
        value = "/webmessage",
        consumes = APPLICATION_JSON_VALUE,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageResult> sendWebMessage(@Valid @RequestBody final WebMessageRequest request,
            @Parameter(description = "Whether to send the message asynchronously")
            @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async,
            final UriComponentsBuilder uriComponentsBuilder) {
        if (async) {
            return toResponse(uriComponentsBuilder, eventDispatcher.handleWebMessageRequest(request));
        } else {
            return toResponse(uriComponentsBuilder, messageService.sendWebMessage(request));
        }
    }

    @Operation(
        summary = "Send a single digital mail to one or more parties",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = MessageBatchResult.class)),
                headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(type = "string"))
            )
        }
    )
    @PostMapping(
        value = "/digital-mail",
        consumes = APPLICATION_JSON_VALUE,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageBatchResult> sendDigitalMail(@Valid @RequestBody final DigitalMailRequest request,
            @Parameter(description = "Whether to send the message asynchronously")
            @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async,
            final UriComponentsBuilder uriComponentsBuilder) {
        if (async) {
            return toResponse(uriComponentsBuilder, eventDispatcher.handleDigitalMailRequest(request));
        } else {
            return toResponse(uriComponentsBuilder, messageService.sendDigitalMail(request));
        }
    }

    @Operation(
        summary = "Send a single snailmail",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = MessageResult.class)),
                headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(type = "string"))
            )
        }
    )
    @PostMapping(
        value = "/snail-mail",
        consumes = APPLICATION_JSON_VALUE,
        produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE}
    )
    ResponseEntity<MessageResult> sendSnailMail(@Valid @RequestBody final SnailMailRequest request,
            @Parameter(description = "Whether to send the message asynchronously")
            @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async,
            final UriComponentsBuilder uriComponentsBuilder) {
        if (async) {
            return toResponse(uriComponentsBuilder, eventDispatcher.handleSnailMailRequest(request));
        } else {
            return toResponse(uriComponentsBuilder, messageService.sendSnailMail(request));
        }
    }

    @Operation(
        summary = "Send a batch of messages as e-mail or SMS to a list of parties",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = MessageBatchResult.class)),
                headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(type = "string"))
            )
        }
    )
    @PostMapping(
        value = "/messages",
        consumes = APPLICATION_JSON_VALUE,
            produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE}
    )
    ResponseEntity<MessageBatchResult> sendMessages(@Valid @RequestBody final MessageRequest request,
            @Parameter(description = "Whether to send the message asynchronously")
            @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async,
            final UriComponentsBuilder uriComponentsBuilder) {
        if (async) {
            return toResponse(uriComponentsBuilder, eventDispatcher.handleMessageRequest(request));
        } else {
            return toResponse(uriComponentsBuilder, messageService.sendMessages(request));
        }
    }

    @Operation(
        summary = "Send a single letter as digital mail or snail mail",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = MessageBatchResult.class)),
                headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(type = "string"))
            )
        }
    )
    @PostMapping(
        value = "/letter",
        consumes = APPLICATION_JSON_VALUE,
        produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE}
    )
    ResponseEntity<MessageBatchResult> sendLetter(@Valid @RequestBody final LetterRequest request,
            @Parameter(description = "Whether to send the message asynchronously")
            @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async,
            final UriComponentsBuilder uriComponentsBuilder) {
        if (async) {
            return toResponse(uriComponentsBuilder, eventDispatcher.handleLetterRequest(request));
        } else {
            return toResponse(uriComponentsBuilder, messageService.sendLetter(request));
        }
    }

    ResponseEntity<MessageResult> toResponse(final UriComponentsBuilder uriComponentsBuilder,
            final InternalDeliveryResult deliveryResult) {
        var uri = uriComponentsBuilder.path(MESSAGE_STATUS_PATH)
            .buildAndExpand(deliveryResult.messageId())
            .toUri();;

        return created(uri)
            .body(MessageResult.builder()
                .withMessageId(deliveryResult.messageId())
                .withDeliveries(List.of(DeliveryResult.builder()
                    .withDeliveryId(deliveryResult.deliveryId())
                    .withMessageType(deliveryResult.messageType())
                    .withStatus(deliveryResult.status())
                    .build()))
                .build());
    }

    ResponseEntity<MessageBatchResult> toResponse(final UriComponentsBuilder uriComponentsBuilder,
            final InternalDeliveryBatchResult deliveryBatchResult) {
        var uri = uriComponentsBuilder.path(BATCH_STATUS_PATH)
            .buildAndExpand(deliveryBatchResult.batchId())
            .toUri();

        // Group the deliveries by message id
        var groupedDeliveries = deliveryBatchResult.deliveries().stream()
            .collect(groupingBy(InternalDeliveryResult::messageId));

        return created(uri)
            .body(MessageBatchResult.builder()
                .withBatchId(deliveryBatchResult.batchId())
                .withMessages(groupedDeliveries.entrySet().stream()
                    .map(message -> MessageResult.builder()
                        .withMessageId(message.getKey())
                        .withDeliveries(message.getValue().stream()
                            .map(delivery -> DeliveryResult.builder()
                                .withDeliveryId(delivery.deliveryId())
                                .withMessageType(delivery.messageType())
                                .withStatus(delivery.status())
                                .build())
                            .toList())
                        .build())
                    .toList())
                .build());
    }
}
