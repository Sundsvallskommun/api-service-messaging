package se.sundsvall.messaging.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import se.sundsvall.dept44.common.validators.annotation.ValidMobileNumber;
import se.sundsvall.messaging.model.PartyWithOptionalPartyId;
import se.sundsvall.messaging.model.Sender;

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
public class SmsRequest extends Request {

    @Valid
    @Schema(description = "Party")
    private PartyWithOptionalPartyId party;

    @Valid
    @Schema(description = "Sender")
    private Sender.Sms sender;

    @ValidMobileNumber
    @Schema(description = "Mobile number. Should start with +467x", requiredMode = REQUIRED)
    private String mobileNumber;

    @NotBlank
    @Schema(description = "Message", requiredMode = REQUIRED)
    private String message;
}
