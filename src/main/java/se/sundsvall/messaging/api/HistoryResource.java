package se.sundsvall.messaging.api;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.messaging.Constants.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.Constants.CONVERSATION_HISTORY_PATH;
import static se.sundsvall.messaging.Constants.DELIVERY_STATUS_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_AND_DELIVERY_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_ATTACHMENT_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_STATUS_PATH;
import static se.sundsvall.messaging.Constants.USER_MESSAGES_PATH;
import static se.sundsvall.messaging.api.model.ApiMapper.toMessageBatchResult;
import static se.sundsvall.messaging.api.model.ApiMapper.toMessageResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
import se.sundsvall.messaging.api.model.response.UserMessages;
import se.sundsvall.messaging.service.HistoryService;

@Tag(name = "History Resources")
@RestController
@Validated
@ApiResponses({
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
})
class HistoryResource {

	private final HistoryService historyService;

	HistoryResource(final HistoryService historyService) {
		this.historyService = historyService;
	}

	@Operation(summary = "Get the entire conversation history for a given party")
	@GetMapping(value = CONVERSATION_HISTORY_PATH, produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<List<HistoryResponse>> getConversationHistory(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String partyId,
		@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "From-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate from,
		@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "To-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate to) {

		return ok(historyService.getConversationHistory(municipalityId, partyId, from, to).stream()
			.map(ApiMapper::toHistoryResponse)
			.toList());
	}

	@Operation(summary = "Get the status for a message batch, its messages and their deliveries")
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping(value = BATCH_STATUS_PATH, produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<MessageBatchResult> getBatchStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String batchId) {

		final var history = historyService.getHistoryByMunicipalityIdAndBatchId(municipalityId, batchId);
		return history.isEmpty() ? notFound().build() : ok(toMessageBatchResult(history));
	}

	@Operation(summary = "Get the status for a single message and its deliveries")
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping(value = MESSAGE_STATUS_PATH, produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<MessageResult> getMessageStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String messageId) {

		final var history = historyService.getHistoryByMunicipalityIdAndMessageId(municipalityId, messageId);
		return history.isEmpty() ? notFound().build() : ok(toMessageResult(history));
	}

	@Operation(summary = "Get the status for a single delivery")
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping(value = DELIVERY_STATUS_PATH, produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<DeliveryResult> getDeliveryStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String deliveryId) {

		return historyService.getHistoryByMunicipalityIdAndDeliveryId(municipalityId, deliveryId)
			.map(ApiMapper::toDeliveryResult)
			.map(ResponseEntity::ok)
			.orElseGet(() -> notFound().build());
	}

	@Operation(summary = "Get a message and all its deliveries")
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping(value = MESSAGE_AND_DELIVERY_PATH, produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<List<HistoryResponse>> getMessage(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String messageId) {

		final var history = historyService.getHistoryByMunicipalityIdAndMessageId(municipalityId, messageId).stream()
			.map(ApiMapper::toHistoryResponse)
			.toList();

		return history.isEmpty() ? notFound().build() : ok(history);
	}

	@Operation(summary = "Get historical messages sent by a user")
	@GetMapping(value = USER_MESSAGES_PATH, produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<UserMessages> getUserMessages(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "userId", description = "User id", example = "test") @PathVariable final String userId,
		@Parameter(name = "page", description = "Which page to fetch", example = "1") @RequestParam(defaultValue = "1") final Integer page,
		@Parameter(name = "limit", description = "Sets the amount of entries per page", example = "1") @RequestParam(defaultValue = "15") final Integer limit) {
		var result = historyService.getUserMessages(municipalityId, userId, page, limit);
		return ok(result);
	}

	@Operation(summary = "Strean attachment by messageId and fileName")
	@GetMapping(value = MESSAGE_ATTACHMENT_PATH, produces = {
		ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	void readAttachment(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String messageId,
		@PathVariable final String fileName,
		final HttpServletResponse response) throws IOException {

		historyService.streamAttachment(municipalityId, messageId, fileName, response);
	}
}
