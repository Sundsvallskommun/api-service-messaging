package se.sundsvall.messaging.api;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.messaging.api.model.ApiMapper.toResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailBatchRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;

@Tag(name = "Sending Resources")
@RestController
@Validated
@RequestMapping(value = "/{municipalityId}", consumes = {
	APPLICATION_JSON_VALUE
}, produces = {
	APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
})
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(schema = @Schema(implementation = Problem.class)))
class MessageResource {

	private final MessageService messageService;

	private final MessageEventDispatcher eventDispatcher;

	MessageResource(final MessageService messageService, final MessageEventDispatcher eventDispatcher) {
		this.messageService = messageService;
		this.eventDispatcher = eventDispatcher;
	}

	@Operation(summary = "Send a single SMS", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/sms")
	ResponseEntity<MessageResult> sendSms(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final SmsRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withIssuer(issuer)
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleSmsRequest(decoratedRequest, municipalityId));
		}
		return toResponse(messageService.sendSms(decoratedRequest, municipalityId));
	}

	@Operation(summary = "Send a batch of sms asynchronously", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/sms/batch")
	ResponseEntity<MessageBatchResult> sendSmsBatch(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final SmsBatchRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {

		return toResponse(eventDispatcher.handleSmsBatchRequest(request
			.withIssuer(issuer)
			.withOrigin(origin), municipalityId));
	}

	@Operation(summary = "Send a single e-mail", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/email")
	ResponseEntity<MessageResult> sendEmail(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final EmailRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withIssuer(issuer)
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleEmailRequest(decoratedRequest, municipalityId));
		}
		return toResponse(messageService.sendEmail(decoratedRequest, municipalityId));
	}

	@Operation(summary = "Send a batch of e-mails asynchronously", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/email/batch")
	ResponseEntity<MessageBatchResult> sendEmailBatch(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final EmailBatchRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {

		return toResponse(eventDispatcher.handleEmailBatchRequest(request
			.withIssuer(issuer)
			.withOrigin(origin), municipalityId));
	}

	@Operation(summary = "Send a single web message", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/webmessage")
	ResponseEntity<MessageResult> sendWebMessage(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final WebMessageRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withIssuer(issuer)
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleWebMessageRequest(decoratedRequest, municipalityId));
		}
		return toResponse(messageService.sendWebMessage(decoratedRequest, municipalityId));
	}

	@Operation(summary = "Send a single digital mail to one or more parties", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/digital-mail")
	ResponseEntity<MessageBatchResult> sendDigitalMail(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final DigitalMailRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withIssuer(issuer)
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleDigitalMailRequest(decoratedRequest, municipalityId));
		}
		return toResponse(messageService.sendDigitalMail(decoratedRequest, municipalityId));
	}

	@Operation(summary = "Send a digital invoice", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/digital-invoice")
	ResponseEntity<MessageResult> sendDigitalInvoice(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final DigitalInvoiceRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withIssuer(issuer)
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleDigitalInvoiceRequest(decoratedRequest, municipalityId));
		}
		return toResponse(messageService.sendDigitalInvoice(decoratedRequest, municipalityId));
	}

	@Operation(summary = "Send a batch of messages as e-mail or SMS to a list of parties", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/messages")
	ResponseEntity<MessageBatchResult> sendMessages(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final MessageRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withIssuer(issuer)
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleMessageRequest(decoratedRequest, municipalityId));
		}
		return toResponse(messageService.sendMessages(decoratedRequest, municipalityId));
	}

	@Operation(summary = "Send a single letter as digital mail or snail mail", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/letter")
	ResponseEntity<MessageBatchResult> sendLetter(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final LetterRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withIssuer(issuer)
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleLetterRequest(decoratedRequest, municipalityId));
		}
		return toResponse(messageService.sendLetter(decoratedRequest, municipalityId));
	}

	@Operation(summary = "Send a single Slack message", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/slack")
	ResponseEntity<MessageResult> sendToSlack(
		@Parameter(name = "x-origin", description = "Origin of the request") @RequestHeader(name = "x-origin", required = false) final String origin,
		@Parameter(name = "x-issuer", description = "Issuer of the request") @RequestHeader(name = "x-issuer", required = false) final String issuer,
		@RequestBody @Valid final SlackRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withIssuer(issuer)
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleSlackRequest(decoratedRequest, municipalityId));
		}
		return toResponse(messageService.sendToSlack(decoratedRequest, municipalityId));
	}

}
