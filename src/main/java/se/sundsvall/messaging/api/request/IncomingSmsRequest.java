package se.sundsvall.messaging.api.request;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import se.sundsvall.messaging.model.Party;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
@Getter
@Setter
@Schema(name = "IncomingSmsRequest", description = "SMS representation")
public class IncomingSmsRequest {

    @NotBlank
    @Size(max = 11)
    @Schema(required = true, description = "The Sender of the SMS", maxLength = 11, example = "sender")
    private String sender;

    @Valid
    @NotNull
    private Party party;

    @NotBlank
    @Schema(required = true, description = "Mobile number should start with +467x")
    @Pattern(regexp = "^\\+467[02369]\\d{7}$")
    private String mobileNumber;

    @NotBlank
    private String message;
}
