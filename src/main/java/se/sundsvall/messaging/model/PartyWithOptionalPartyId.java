package se.sundsvall.messaging.model;

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
public class PartyWithOptionalPartyId extends Party {

    @ValidUuid(nullable = true)
    @Schema(description = "The message party ID", example = "f427952b-247c-4d3b-b081-675a467b3619")
    private String partyId;
}
