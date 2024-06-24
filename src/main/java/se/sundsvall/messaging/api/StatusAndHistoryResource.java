package se.sundsvall.messaging.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.messaging.Constants.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.Constants.CONVERSATION_HISTORY_PATH;
import static se.sundsvall.messaging.Constants.DELIVERY_STATUS_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_STATUS_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_AND_DELIVERY_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_FOR_DEPARTMENTS_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_PATH;
import static se.sundsvall.messaging.api.model.ApiMapper.toMessageBatchResult;
import static se.sundsvall.messaging.api.model.ApiMapper.toMessageResult;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.api.model.ApiMapper;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.HistoryResponse;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.api.validation.ValidNullOrNotEmpty;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.service.StatisticsService;

@Tag(name = "Status and History Resources")
@RestController
@Validated
class StatusAndHistoryResource {

	private final HistoryService historyService;
	private final StatisticsService statisticsService;

	StatusAndHistoryResource(final HistoryService historyService,
		final StatisticsService statisticsService) {
		this.historyService = historyService;
		this.statisticsService = statisticsService;
	}

	@Operation(summary = "Get the entire conversation history for a given party", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoryResponse.class)))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(value = CONVERSATION_HISTORY_PATH, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<List<HistoryResponse>> getConversationHistory(
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String partyId,
		@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "From-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate from,
		@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "To-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate to) {

		return ResponseEntity.ok(historyService.getConversationHistory(partyId, from, to).stream()
			.map(ApiMapper::toHistoryResponse)
			.toList());
	}

	@Operation(summary = "Get the status for a message batch, its messages and their deliveries", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", content = @Content(schema = @Schema(implementation = MessageBatchResult.class))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(value = BATCH_STATUS_PATH, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<MessageBatchResult> getBatchStatus(
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String batchId) {

		final var history = historyService.getHistoryByBatchId(batchId);
		return history.isEmpty() ? 
			ResponseEntity.notFound().build() : 
			ResponseEntity.ok(toMessageBatchResult(history));
	}

	@Operation(summary = "Get the status for a single message and its deliveries", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", content = @Content(schema = @Schema(implementation = MessageResult.class))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(value = MESSAGE_STATUS_PATH, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<MessageResult> getMessageStatus(
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String messageId) {

		final var history = historyService.getHistoryByMessageId(messageId);
		return history.isEmpty() ? 
			ResponseEntity.notFound().build() :
			ResponseEntity.ok(toMessageResult(history));
	}

	@Operation(summary = "Get the status for a single delivery", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", content = @Content(schema = @Schema(implementation = DeliveryResult.class))),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(value = DELIVERY_STATUS_PATH, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<DeliveryResult> getDeliveryStatus(
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String deliveryId) {

		return historyService.getHistoryByDeliveryId(deliveryId)
			.map(ApiMapper::toDeliveryResult)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Get a message and all its deliveries", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistoryResponse.class)))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(value = MESSAGE_AND_DELIVERY_PATH, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<List<HistoryResponse>> getMessage(
		@Parameter(schema = @Schema(format = "uuid")) @PathVariable @ValidUuid final String messageId) {

		final var history = historyService.getHistoryByMessageId(messageId).stream()
			.map(ApiMapper::toHistoryResponse)
			.toList();

		return history.isEmpty() ?
			ResponseEntity.notFound().build() :
			ResponseEntity.ok(history);
	}

	@Operation(summary = "Get delivery statistics", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Statistics.class)))),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(value = STATISTICS_PATH, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Statistics> getStatistics(
		@RequestParam(name = "messageType", required = false) @Parameter(description = "Message type") final MessageType messageType,
		@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "From-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate from,
		@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "To-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate to) {

		return ResponseEntity.ok(statisticsService.getStatistics(messageType, from, to));
	}

	@Operation(summary = "Get letter delivery statistics by department", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(value = { STATISTICS_FOR_DEPARTMENTS_PATH, STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH }, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<List<DepartmentStatistics>> getDepartmentStatistics(
		@PathVariable(name = "department", required = false) @Parameter(description = "Department name") @ValidNullOrNotEmpty final String department,
		@RequestParam(name = "origin", required = false) @Parameter(description = "Origin name") final String origin,
		@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "From-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate from,
		@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "To-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate to) {

		return ResponseEntity.ok(statisticsService.getDepartmentLetterStatistics(origin, department, from, to));
	}
}
