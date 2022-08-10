package se.sundsvall.messaging.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

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
public class Parties {

    @NotEmpty
    @Schema(description = "The message party ID:s")
    private List<@ValidUuid String> partyIds;

    @Schema(description = "External references")
    private List<@Valid ExternalReference> externalReferences;
}