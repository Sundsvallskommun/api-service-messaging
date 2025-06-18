package se.sundsvall.messaging.api;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.messaging.Constants.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.Constants.CONVERSATION_HISTORY_PATH;
import static se.sundsvall.messaging.Constants.DELIVERY_STATUS_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_AND_DELIVERY_METADATA_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_AND_DELIVERY_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_ATTACHMENT_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_STATUS_PATH;
import static se.sundsvall.messaging.Constants.USER_BATCHES_PATH;
import static se.sundsvall.messaging.Constants.USER_MESSAGES_PATH;
import static se.sundsvall.messaging.Constants.USER_MESSAGE_PATH;
import static se.sundsvall.messaging.api.model.ApiMapper.toMessageBatchResult;
import static se.sundsvall.messaging.api.model.ApiMapper.toMessageResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.api.model.ApiMapper;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.HistoryResponse;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.api.model.response.UserBatches;
import se.sundsvall.messaging.api.model.response.UserMessage;
import se.sundsvall.messaging.api.model.response.UserMessages;
import se.sundsvall.messaging.service.HistoryService;

@Tag(name = "History Resources")
@RestController
@Validated
@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
@ApiResponse(responseCode = "400",
	description = "Bad Request",
	content = @Content(
		mediaType = APPLICATION_PROBLEM_JSON_VALUE,
		schema = @Schema(oneOf = {
			Problem.class, ConstraintViolationProblem.class,
		})))
@ApiResponse(responseCode = "500",
	description = "Internal Server Error",
	content = @Content(
		mediaType = APPLICATION_PROBLEM_JSON_VALUE,
		schema = @Schema(implementation = Problem.class)))
class HistoryResource {

	private final HistoryService historyService;

	HistoryResource(final HistoryService historyService) {
		this.historyService = historyService;
	}

	@Operation(summary = "Get the entire conversation history for a given party")
	@GetMapping(value = CONVERSATION_HISTORY_PATH, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<HistoryResponse>> getConversationHistory(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", schema = @Schema(format = "uuid"), example = "46f9bf9f-09f2-45ac-8c8b-6cad847541ed") @PathVariable @ValidUuid final String partyId,
		@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "From-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate from,
		@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "To-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate to) {

		return ok(historyService.getConversationHistory(municipalityId, partyId, from, to).stream()
			.map(ApiMapper::toHistoryResponse)
			.toList());
	}

	@Operation(summary = "Get the status for a message batch, its messages and their deliveries",
		responses = {
			@ApiResponse(responseCode = "404",
				description = "Not Found",
				content = @Content(
					mediaType = APPLICATION_PROBLEM_JSON_VALUE,
					schema = @Schema(implementation = Problem.class)))
		})
	@GetMapping(value = BATCH_STATUS_PATH, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<MessageBatchResult> getBatchStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "batchId", schema = @Schema(format = "uuid"), example = "118e05b3-4321-4f46-9a33-b9f43faa58a6") @PathVariable @ValidUuid final String batchId) {

		final var history = historyService.getHistoryByMunicipalityIdAndBatchId(municipalityId, batchId);
		return history.isEmpty() ? notFound().build() : ok(toMessageBatchResult(history));
	}

	@Operation(summary = "Get the status for a single message and its deliveries",
		responses = {
			@ApiResponse(responseCode = "404",
				description = "Not Found",
				content = @Content(
					mediaType = APPLICATION_PROBLEM_JSON_VALUE,
					schema = @Schema(implementation = Problem.class)))
		})
	@GetMapping(value = MESSAGES_STATUS_PATH, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<MessageResult> getMessageStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "messageId", schema = @Schema(format = "uuid"), example = "d1e07d2c-2e75-44e0-b978-de7e19d7edad") @PathVariable @ValidUuid final String messageId) {

		final var history = historyService.getHistoryByMunicipalityIdAndMessageId(municipalityId, messageId);
		return history.isEmpty() ? notFound().build() : ok(toMessageResult(history));
	}

	@Operation(summary = "Get the status for a single delivery",
		responses = {
			@ApiResponse(responseCode = "404",
				description = "Not Found",
				content = @Content(
					mediaType = APPLICATION_PROBLEM_JSON_VALUE,
					schema = @Schema(implementation = Problem.class)))
		})
	@GetMapping(value = DELIVERY_STATUS_PATH, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<DeliveryResult> getDeliveryStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "deliveryId", schema = @Schema(format = "uuid"), example = "41141c6f-4825-45a7-b52b-79fbff70765e") @PathVariable @ValidUuid final String deliveryId) {

		return historyService.getHistoryByMunicipalityIdAndDeliveryId(municipalityId, deliveryId)
			.map(ApiMapper::toDeliveryResult)
			.map(ResponseEntity::ok)
			.orElseGet(() -> notFound().build());
	}

	/**
	 * @deprecated Use {@link #getMessageMetadata(String, String)} instead.
	 */
	@Deprecated(forRemoval = true, since = "2025-04-28")
	@Operation(summary = "Get a message and all its deliveries",
		deprecated = true,
		description = "This endpoint is deprecated and will be removed in a future version."
			+ "Use /messages/{messageId}/metadata instead. To get the file content for the message use /messages/{messageId}/attachments/{fileName}.",
		responses = {
			@ApiResponse(responseCode = "404",
				description = "Not Found",
				content = @Content(
					mediaType = APPLICATION_PROBLEM_JSON_VALUE,
					schema = @Schema(implementation = Problem.class)))
		})
	@GetMapping(value = MESSAGES_AND_DELIVERY_PATH, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<HistoryResponse>> getMessage(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "messageId", schema = @Schema(format = "uuid"), example = "d1e07d2c-2e75-44e0-b978-de7e19d7edad") @PathVariable @ValidUuid final String messageId) {

		final var history = historyService.getHistoryByMunicipalityIdAndMessageId(municipalityId, messageId).stream()
			.map(ApiMapper::toHistoryResponse)
			.toList();

		return history.isEmpty() ? notFound().build() : ok(history);
	}

	@Operation(summary = "Get metadata for a message and all its deliveries",
		responses = {
			@ApiResponse(responseCode = "404",
				description = "Not Found",
				content = @Content(
					mediaType = APPLICATION_PROBLEM_JSON_VALUE,
					schema = @Schema(implementation = Problem.class)))
		})
	@GetMapping(value = MESSAGES_AND_DELIVERY_METADATA_PATH, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<HistoryResponse>> getMessageMetadata(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "messageId", schema = @Schema(format = "uuid"), example = "d1e07d2c-2e75-44e0-b978-de7e19d7edad") @PathVariable @ValidUuid final String messageId) {

		final var history = historyService.getHistoryByMunicipalityIdAndMessageId(municipalityId, messageId).stream()
			.map(ApiMapper::toMetadataHistoryResponse)
			.toList();

		return history.isEmpty() ? notFound().build() : ok(history);
	}

	@Operation(summary = "Get information regarding historical batches sent by a user")
	@GetMapping(value = USER_BATCHES_PATH, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<UserBatches> getUserBatches(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "userId", description = "User id", example = "joe01doe") @PathVariable final String userId,
		@Parameter(name = "page", description = "Which page to fetch", example = "1") @RequestParam(defaultValue = "1") @Min(value = 1, message = "must be greater than or equal to 1 and less than or equal to 2147483647") final Integer page,
		@Parameter(name = "limit", description = "Sets the amount of entries per page", example = "1") @RequestParam(defaultValue = "15") @Min(value = 1,
			message = "must be greater than or equal to 1 and less than or equal to 2147483647") final Integer limit) {

		return ok(historyService.getUserBatches(municipalityId, userId, page, limit));
	}

	@Operation(summary = "Get historical messages sent by a user, optionally filtered by batch id")
	@GetMapping(value = USER_MESSAGES_PATH, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<UserMessages> getUserMessages(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "userId", description = "User id", example = "joe01doe") @PathVariable final String userId,
		@Parameter(name = "batchId", schema = @Schema(format = "uuid"), description = "Batch id", example = "118e05b3-4321-4f46-9a33-b9f43faa58a6") @RequestParam(required = false) @ValidUuid(nullable = true) final String batchId,
		@Parameter(name = "page", description = "Which page to fetch", example = "1") @RequestParam(defaultValue = "1") @Min(value = 1, message = "must be greater than or equal to 1 and less than or equal to 2147483647") final Integer page,
		@Parameter(name = "limit", description = "Sets the amount of entries per page", example = "1") @RequestParam(defaultValue = "15") @Min(value = 1,
			message = "must be greater than or equal to 1 and less than or equal to 2147483647") final Integer limit) {

		return ok(historyService.getUserMessages(municipalityId, userId, batchId, page, limit));
	}

	@Operation(summary = "Get a historical message sent by a user")
	@GetMapping(value = USER_MESSAGE_PATH, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<UserMessage> getUserMessage(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "userId", description = "User id", example = "joe01doe") @PathVariable final String userId,
		@Parameter(name = "messageId", schema = @Schema(format = "uuid"), example = "d1e07d2c-2e75-44e0-b978-de7e19d7edad") @PathVariable @ValidUuid final String messageId) {

		return ok(historyService.getUserMessage(municipalityId, userId, messageId));
	}

	@Operation(summary = "Stream attachment by messageId and fileName")
	@GetMapping(value = MESSAGES_ATTACHMENT_PATH, produces = ALL_VALUE)
	void readAttachment(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "messageId", schema = @Schema(format = "uuid"), example = "d1e07d2c-2e75-44e0-b978-de7e19d7edad") @PathVariable @ValidUuid final String messageId,
		@Parameter(name = "fileName", example = "some-filename.txt") @PathVariable final String fileName,
		final HttpServletResponse response) throws IOException {

		historyService.streamAttachment(municipalityId, messageId, fileName, response);
	}
}
