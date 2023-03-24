package se.sundsvall.messaging.api.model.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import se.sundsvall.dept44.common.validators.annotation.ValidMobileNumber;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Header;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record SmsRequest(

        @Valid
        @Schema(description = "Party")
        Party party,

        @Schema(description = "Headers")
        List<@Valid Header> headers,

        @Schema(description = "Sender")
        String sender,

        @ValidMobileNumber
        @Schema(description = "Mobile number. Should start with +467x", requiredMode = REQUIRED)
        String mobileNumber,

        @NotBlank
        @Schema(description = "Message", requiredMode = REQUIRED)
        String message) {

    @With
    @Builder(setterPrefix = "with")
    @Schema(name = "SmsRequestParty")
    public record Party(

            @ValidUuid(nullable = true)
            @Schema(description = "The message party id", example = "f427952b-247c-4d3b-b081-675a467b3619")
            String partyId,

            @Schema(description = "External references")
            List<@Valid ExternalReference> externalReferences) {
    }
}