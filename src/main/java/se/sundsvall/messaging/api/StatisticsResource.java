package se.sundsvall.messaging.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.messaging.Constants.STATISTICS_FOR_DEPARTMENTS_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_PATH;

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
import se.sundsvall.messaging.api.validation.ValidNullOrNotEmpty;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;
import se.sundsvall.messaging.service.StatisticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Statistics Resources")
@Validated
@RestController
@ApiResponses({
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(oneOf = {
		Problem.class, ConstraintViolationProblem.class
	}))),
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
})
class StatisticsResource {

	private final StatisticsService statisticsService;

	StatisticsResource(final StatisticsService statisticsService) {
		this.statisticsService = statisticsService;
	}

	@Operation(summary = "Get delivery statistics")
	@GetMapping(value = STATISTICS_PATH, produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<Statistics> getStatistics(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestParam(name = "messageType", required = false) @Parameter(description = "Message type") final MessageType messageType,
		@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "From-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate from,
		@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "To-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate to) {

		return ok(statisticsService.getStatistics(messageType, from, to, municipalityId));
	}

	@Operation(summary = "Get letter delivery statistics by department")
	@GetMapping(value = {
		STATISTICS_FOR_DEPARTMENTS_PATH, STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH
	}, produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	ResponseEntity<List<DepartmentStatistics>> getDepartmentStatistics(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable(name = "department", required = false) @Parameter(description = "Department name") @ValidNullOrNotEmpty final String department,
		@RequestParam(name = "origin", required = false) @Parameter(description = "Origin name") final String origin,
		@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "From-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate from,
		@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "To-date (inclusive). Format: yyyy-MM-dd (ISO8601)") final LocalDate to) {

		return ok(statisticsService.getDepartmentLetterStatistics(origin, department, from, to, municipalityId));
	}

}
