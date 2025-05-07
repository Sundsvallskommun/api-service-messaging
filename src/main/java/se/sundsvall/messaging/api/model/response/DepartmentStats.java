package se.sundsvall.messaging.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import se.sundsvall.messaging.model.Count;

@Builder(setterPrefix = "with")
@Schema(name = "DepartmentStats")
public record DepartmentStats(
	@JsonProperty("ORIGIN") String origin,
	@JsonProperty("DEPARTMENT") String department,
	@JsonProperty("SNAIL_MAIL") Count snailMail,
	@JsonProperty("DIGITAL_MAIL") Count digitalMail,
	@JsonProperty("SMS") Count sms) {
}
