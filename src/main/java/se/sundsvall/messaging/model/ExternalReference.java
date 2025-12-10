package se.sundsvall.messaging.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record ExternalReference(

	@NotBlank @Schema(description = "The external reference key", examples = "flowInstanceId") String key,

	@NotBlank @Schema(description = "The external reference value", examples = "356t4r34f") String value) {}
