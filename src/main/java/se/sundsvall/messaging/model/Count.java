package se.sundsvall.messaging.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with")
@Schema(name = "StatisticsCounter")
public record Count(int sent, int failed) {

	public int total() {
		return sent + failed;
	}
}
