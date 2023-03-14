package se.sundsvall.messaging.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
    @NotNull
    @Schema(description = "Parties", requiredMode = REQUIRED)
    private Parties party;

    @Schema(description = "Headers")
    private List<@Valid Header> headers;
}
