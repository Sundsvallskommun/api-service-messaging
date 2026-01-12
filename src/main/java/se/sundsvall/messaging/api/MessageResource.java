package se.sundsvall.messaging.api;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.messaging.Constants.X_ISSUER_HEADER_KEY;
import static se.sundsvall.messaging.Constants.X_ORIGIN_HEADER_KEY;
import static se.sundsvall.messaging.api.model.ApiMapper.toResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.UniqueElements;
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
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidOrganizationNumber;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailBatchRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.Mailbox;
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
	APPLICATION_JSON_VALUE
})
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class MessageResource {

	private static final String SUNDSVALLS_MUNICIPALITY_ORGANIZATION_NUMBER = "2120002411";

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
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final SmsRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleSmsRequest(decoratedRequest));
		}
		return toResponse(messageService.sendSms(decoratedRequest));
	}

	@Operation(summary = "Send a batch of sms asynchronously", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/sms/batch")
	ResponseEntity<MessageBatchResult> sendSmsBatch(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final SmsBatchRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		return toResponse(eventDispatcher.handleSmsBatchRequest(decoratedRequest));
	}

	@Operation(summary = "Send a single e-mail", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/email")
	ResponseEntity<MessageResult> sendEmail(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final EmailRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleEmailRequest(decoratedRequest));
		}
		return toResponse(messageService.sendEmail(decoratedRequest));
	}

	@Operation(summary = "Send a batch of e-mails asynchronously", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/email/batch")
	ResponseEntity<MessageBatchResult> sendEmailBatch(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final EmailBatchRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		return toResponse(eventDispatcher.handleEmailBatchRequest(decoratedRequest));
	}

	@Operation(summary = "Send a single web message", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/webmessage")
	ResponseEntity<MessageResult> sendWebMessage(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final WebMessageRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleWebMessageRequest(decoratedRequest));
		}
		return toResponse(messageService.sendWebMessage(decoratedRequest));
	}

	@Operation(summary = "Send a single digital mail to one or more parties", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/{organizationNumber}/digital-mail")
	ResponseEntity<MessageBatchResult> sendDigitalMail(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@RequestBody @Valid final DigitalMailRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "organizationNumber", description = "The organization number of the sending organization", example = "5561234567") @ValidOrganizationNumber @PathVariable final String organizationNumber,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(null))
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleDigitalMailRequest(decoratedRequest, organizationNumber));
		}
		return toResponse(messageService.sendDigitalMail(decoratedRequest, organizationNumber));
	}

	/**
	 * @deprecated since 2025-09-15, will be removed in a future version. Use
	 *             {@link #sendDigitalMail(String, DigitalMailRequest, String, String, boolean)} instead.
	 */
	@Deprecated(forRemoval = true, since = "2025-09-15")
	@Operation(summary = "Send a single digital mail to one or more parties",
		deprecated = true,
		description = "This endpoint is deprecated in favor of /{organizationNumber}/digital-mail and will be removed in a future version.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
		})
	@PostMapping("/digital-mail")
	ResponseEntity<MessageBatchResult> sendDigitalMailDeprecated(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final DigitalMailRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		// To keep backwards compatibility we default to Sundsvall municipalitys organization number since the new
		// api in digital mail sender requires an organization number
		if (async) {
			return toResponse(eventDispatcher.handleDigitalMailRequest(decoratedRequest, SUNDSVALLS_MUNICIPALITY_ORGANIZATION_NUMBER));
		}
		return toResponse(messageService.sendDigitalMail(decoratedRequest, SUNDSVALLS_MUNICIPALITY_ORGANIZATION_NUMBER));
	}

	@Operation(summary = "Retrieve a list of digital mailboxes",
		description = "Response contains a list of partyIds, supplier and if the digital mailbox is reachable for the given organization.",
		responses = {
			@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
		})
	@PostMapping(value = "/{organizationNumber}/mailboxes", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<Mailbox>> getMailboxes(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "organizationNumber", description = "The organization number of the intended sending organization", example = "5561234567") @ValidOrganizationNumber @PathVariable final String organizationNumber,
		@RequestBody @UniqueElements @NotEmpty final List<@ValidUuid String> partyIds) {

		return ok().body(messageService.getMailboxes(municipalityId, organizationNumber, partyIds));
	}

	@Operation(summary = "Send a digital invoice", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/digital-invoice")
	ResponseEntity<MessageResult> sendDigitalInvoice(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final DigitalInvoiceRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleDigitalInvoiceRequest(decoratedRequest));
		}
		return toResponse(messageService.sendDigitalInvoice(decoratedRequest));
	}

	@Operation(summary = "Send a batch of messages as e-mail or SMS to a list of parties", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/messages")
	ResponseEntity<MessageBatchResult> sendMessages(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final MessageRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleMessageRequest(decoratedRequest));
		}
		return toResponse(messageService.sendMessages(decoratedRequest));
	}

	@Operation(summary = "Send a single letter as digital mail or snail mail", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/letter")
	ResponseEntity<MessageBatchResult> sendLetter(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final LetterRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		// To keep backwards compatibility we default to Sundsvall municipalitys organization number since the new
		// api in digital mail sender requires an organization number
		if (async) {
			return toResponse(eventDispatcher.handleLetterRequest(decoratedRequest, SUNDSVALLS_MUNICIPALITY_ORGANIZATION_NUMBER));
		}
		return toResponse(messageService.sendLetter(decoratedRequest, SUNDSVALLS_MUNICIPALITY_ORGANIZATION_NUMBER));
	}

	@Operation(summary = "Send a single Slack message", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/slack")
	ResponseEntity<MessageResult> sendToSlack(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@Deprecated(since = "2025-05-22", forRemoval = true) @Parameter(name = X_ISSUER_HEADER_KEY,
			deprecated = true,
			description = "Issuer of the request.  **DEPRECATED**: This parameter will be removed in a future version, use X-Sent-By instead.") @RequestHeader(name = X_ISSUER_HEADER_KEY, required = false) final String issuer,
		@RequestBody @Valid final SlackRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(description = "If the message should be sent asynchronously or not") @RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(resolveSentBy(issuer)) // Replace with sentBy when old issuer header is removed
			.withOrigin(origin);

		if (async) {
			return toResponse(eventDispatcher.handleSlackRequest(decoratedRequest));
		}
		return toResponse(messageService.sendToSlack(decoratedRequest));
	}

	@Operation(summary = "Add snail-mail to a batch", responses = {
		@ApiResponse(responseCode = "201", description = "Successful Operation", useReturnTypeSchema = true, headers = @Header(name = LOCATION, schema = @Schema(type = "string")))
	})
	@PostMapping("/snail-mail")
	ResponseEntity<MessageResult> addSnailMailToBatch(
		@Parameter(name = X_ORIGIN_HEADER_KEY, description = "Origin of the request") @RequestHeader(name = X_ORIGIN_HEADER_KEY, required = false) final String origin,
		@RequestBody @Valid final SnailMailRequest request,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "batchId", description = "The snail-mail batch id", example = "f427952b-247c-4d3b-b081-675a467b3619") @RequestParam(name = "batchId") @ValidUuid final String batchId) {

		var sentBy = Optional.ofNullable(Identifier.get()).map(Identifier::getValue).orElse(null);
		final var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withOrigin(origin)
			.withIssuer(sentBy);

		return toResponse(messageService.sendSnailMail(decoratedRequest, batchId));
	}

	@Operation(summary = "Trigger processing of a snail-mail batch", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@PostMapping(value = "/snail-mail/batch/{batchId}", consumes = ALL_VALUE)
	ResponseEntity<Void> triggerSnailMailBatchProcessing(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "batchId", description = "The snail-mail batch id", example = "f427952b-247c-4d3b-b081-675a467b3619") @ValidUuid @PathVariable final String batchId) {

		messageService.sendSnailMailBatch(municipalityId, batchId);
		return ok().build();
	}

	// Determine the value of the "sentBy" header, if present uses it, otherwise try to get the value from the
	// x-issuer-header
	private String resolveSentBy(final String issuer) {
		return Optional.ofNullable(Identifier.get())
			.map(Identifier::getValue)
			.orElseGet(() -> StringUtils.isNotBlank(issuer) ? issuer : null);
	}
}
