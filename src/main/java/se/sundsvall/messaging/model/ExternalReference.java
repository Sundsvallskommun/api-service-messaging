package se.sundsvall.messaging.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExternalReference {

    @Schema(description = "The external reference key", example = "flowInstanceId")
    private String key;

    @Schema(description = "The external reference value", example = "356t4r34f")
    private String value;
}
