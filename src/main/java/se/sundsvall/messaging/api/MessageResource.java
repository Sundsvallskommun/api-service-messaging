package se.sundsvall.messaging.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;

import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.messaging.api.StatusAndHistoryResource.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.api.StatusAndHistoryResource.MESSAGE_STATUS_PATH;

@Tag(name = "Sending Resources")
@RestController
@RequestMapping(
	consumes = {APPLICATION_JSON_VALUE},
	produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE}
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

	@Operation(
		summary = "Send a single SMS",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Successful Operation",
				useReturnTypeSchema = true,
				headers = @Header(name = LOCATION, schema = @Schema(type = "string"))
			)
		}
	)
	@PostMapping("/sms")
	ResponseEntity<MessageResult> sendSms(@RequestHeader(value = "x-origin", required = false) String origin, @Valid @RequestBody final SmsRequest request,
		@Parameter(description = "Whether to send the message asynchronously")
		@RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {
		if (async) {
			return toResponse(eventDispatcher.handleSmsRequest(request.withOrigin(origin)));
		}

		return toResponse(messageService.sendSms(request.withOrigin(origin)));
	}

	@Operation(
		summary = "Send a single e-mail",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Successful Operation",
				useReturnTypeSchema = true,
				headers = @Header(name = LOCATION, schema = @Schema(type = "string"))
			)
		}
	)
	@PostMapping("/email")
	ResponseEntity<MessageResult> sendEmail(@RequestHeader(value = "x-origin", required = false) String origin, @Valid @RequestBody final EmailRequest request,
		@Parameter(description = "Whether to send the message asynchronously")
		@RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {
		if (async) {
			return toResponse(eventDispatcher.handleEmailRequest(request.withOrigin(origin)));
		}

		return toResponse(messageService.sendEmail(request.withOrigin(origin)));
	}

	@Operation(
		summary = "Send a single web message",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Successful Operation",
				useReturnTypeSchema = true,
				headers = @Header(name = LOCATION, schema = @Schema(type = "string"))
			)
		}
	)
	@PostMapping("/webmessage")
	ResponseEntity<MessageResult> sendWebMessage(@RequestHeader(value = "x-origin", required = false) String origin, @Valid @RequestBody final WebMessageRequest request,
		@Parameter(description = "Whether to send the message asynchronously")
		@RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {
		if (async) {
			return toResponse(eventDispatcher.handleWebMessageRequest(request.withOrigin(origin)));
		}

		return toResponse(messageService.sendWebMessage(request.withOrigin(origin)));
	}

	@Operation(
		summary = "Send a single digital mail to one or more parties",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Successful Operation",
				useReturnTypeSchema = true,
				headers = @Header(name = LOCATION, schema = @Schema(type = "string"))
			)
		}
	)
	@PostMapping("/digital-mail")
	ResponseEntity<MessageBatchResult> sendDigitalMail(@RequestHeader(value = "x-origin", required = false) String origin, @Valid @RequestBody final DigitalMailRequest request,
		@Parameter(description = "Whether to send the message asynchronously")
		@RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {
		if (async) {
			return toResponse(eventDispatcher.handleDigitalMailRequest(request.withOrigin(origin)));
		}

		return toResponse(messageService.sendDigitalMail(request.withOrigin(origin)));
	}

	@Operation(
		summary = "Send a digital invoice",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Successful Operation",
				useReturnTypeSchema = true,
				headers = @Header(name = LOCATION, schema = @Schema(type = "string"))
			)
		}
	)
	@PostMapping("/digital-invoice")
	ResponseEntity<MessageResult> sendDigitalInvoice(@RequestHeader(value = "x-origin", required = false) final String origin, @Valid @RequestBody final DigitalInvoiceRequest request,
		@Parameter(description = "Whether to send the message asynchronously")
		@RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {
		if (async) {
			return toResponse(eventDispatcher.handleDigitalInvoiceRequest(request.withOrigin(origin)));
		}

		return toResponse(messageService.sendDigitalInvoice(request.withOrigin(origin)));
	}

	@Operation(
		summary = "Send a batch of messages as e-mail or SMS to a list of parties",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Successful Operation",
				useReturnTypeSchema = true,
				headers = @Header(name = LOCATION, schema = @Schema(type = "string"))
			)
		}
	)
	@PostMapping("/messages")
	ResponseEntity<MessageBatchResult> sendMessages(@RequestHeader(value = "x-origin", required = false) final String origin, @Valid @RequestBody final MessageRequest request,
		@Parameter(description = "Whether to send the message asynchronously")
		@RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {
		if (async) {
			return toResponse(eventDispatcher.handleMessageRequest(request.withOrigin(origin)));
		}

		return toResponse(messageService.sendMessages(request.withOrigin(origin)));
	}

	@Operation(
		summary = "Send a single letter as digital mail or snail mail",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Successful Operation",
				useReturnTypeSchema = true,
				headers = @Header(name = LOCATION, schema = @Schema(type = "string"))
			)
		}
	)
	@PostMapping("/letter")
	ResponseEntity<MessageBatchResult> sendLetter(@RequestHeader(value = "x-origin", required = false) String origin, @Valid @RequestBody final LetterRequest request,
		@Parameter(description = "Whether to send the message asynchronously")
		@RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {

		if (async) {
			return toResponse(eventDispatcher.handleLetterRequest(request.withOrigin(origin)));
		}

		return toResponse(messageService.sendLetter(request.withOrigin(origin)));
	}

	@Operation(
		summary = "Send a single Slack message",
		responses = {
			@ApiResponse(
				responseCode = "201",
				description = "Successful Operation",
				useReturnTypeSchema = true,
				headers = @Header(name = LOCATION, schema = @Schema(type = "string"))
			)
		}
	)
	@PostMapping("/slack")
	ResponseEntity<MessageResult> sendToSlack(@RequestHeader(value = "x-origin", required = false) final String origin, @Valid @RequestBody final SlackRequest request,
		@Parameter(description = "Whether to send the message asynchronously")
		@RequestParam(name = "async", required = false, defaultValue = "false") final boolean async) {
		if (async) {
			return toResponse(eventDispatcher.handleSlackRequest(request.withOrigin(origin)));
		}

		return toResponse(messageService.sendToSlack(request.withOrigin(origin)));
	}

	ResponseEntity<MessageResult> toResponse(final InternalDeliveryResult deliveryResult) {
		var uri = fromPath(MESSAGE_STATUS_PATH)
			.buildAndExpand(deliveryResult.messageId())
			.toUri();

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

	ResponseEntity<MessageBatchResult> toResponse(final InternalDeliveryBatchResult deliveryBatchResult) {
		var uri = fromPath(BATCH_STATUS_PATH)
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
