package se.sundsvall.messaging.model;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record ExternalReference(

    @NotBlank
    @Schema(description = "The external reference key", example = "flowInstanceId")
    String key,

    @NotBlank
    @Schema(description = "The external reference value", example = "356t4r34f")
    String value) { }
