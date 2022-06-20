package se.sundsvall.messaging.api.model;

import java.util.List;

import javax.validation.Valid;

import se.sundsvall.messaging.model.Header;
import se.sundsvall.messaging.model.Parties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract class BatchRequest {

    @Valid
    @Schema(description = "Parties")
    private Parties party;

    @Schema(description = "Headers")
    private List<@Valid Header> headers;
}
