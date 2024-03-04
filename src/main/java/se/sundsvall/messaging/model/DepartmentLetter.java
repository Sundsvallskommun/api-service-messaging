package se.sundsvall.messaging.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with")
@Schema(name = "DepartmentLetterStatistics")
public record DepartmentLetter(

	@JsonProperty("DEPARTMENT")
	String department,
	@JsonProperty("SNAIL_MAIL")
	Count snailMail,
	@JsonProperty("DIGITAL_MAIL")
	Count digitalMail) {
}

