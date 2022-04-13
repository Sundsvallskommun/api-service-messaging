package se.sundsvall.messaging.model;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExternalReference {

    @NotBlank
    @Schema(description = "The external reference key", example = "flowInstanceId")
    private String key;

    @NotBlank
    @Schema(description = "The external reference value", example = "356t4r34f")
    private String value;
}
