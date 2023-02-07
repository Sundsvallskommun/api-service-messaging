package se.sundsvall.messaging.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.messaging.api.ResultMapper.toResponse;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
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

    MessageResource(final MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "Send a single SMS")
    @PostMapping(
        value = "/sms",
        consumes = APPLICATION_JSON_VALUE,
        produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE }
    )
    ResponseEntity<MessageResult> sendSms(@Valid @RequestBody final SmsRequest request) {
        return toResponse(messageService.sendSms(request));
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
    ResponseEntity<MessageResult> sendEmail(@Valid @RequestBody final EmailRequest request) {
        return toResponse(messageService.sendEmail(request));
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
    ResponseEntity<MessageResult> sendWebMessage(@Valid @RequestBody final WebMessageRequest request) {
        return toResponse(messageService.sendWebMessage(request));
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
    ResponseEntity<MessageBatchResult> sendDigitalMail(@Valid @RequestBody final DigitalMailRequest request) {
        return toResponse(messageService.sendDigitalMail(request));
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
    ResponseEntity<MessageResult> sendSnailMail(@Valid @RequestBody final SnailMailRequest request) {
        return toResponse(messageService.sendSnailMail(request));
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
    ResponseEntity<MessageBatchResult> sendMessages(@Valid @RequestBody final MessageRequest request) {
        return toResponse(messageService.sendMessages(request));
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
    ResponseEntity<MessageBatchResult> sendLetter(@Valid @RequestBody final LetterRequest request) {
        return toResponse(messageService.sendLetter(request));
    }
}
