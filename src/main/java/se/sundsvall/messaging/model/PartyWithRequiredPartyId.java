package se.sundsvall.messaging.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PartyWithRequiredPartyId extends Party {

    @ValidUuid
    @Schema(
        description = "The message party ID",
        example = "f427952b-247c-4d3b-b081-675a467b3619",
        requiredMode = REQUIRED
    )
    private String partyId;
}
