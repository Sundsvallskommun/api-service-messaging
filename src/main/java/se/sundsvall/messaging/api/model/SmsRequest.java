package se.sundsvall.messaging.api.model;

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

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SmsRequest {

    @NotBlank
    @Size(max = 11)
    @Schema(required = true, description = "The sender of the SMS", maxLength = 11, example = "sender")
    private String sender;

    @Valid
    @NotNull
    private Party party;

    @NotBlank
    @Schema(required = true, description = "Mobile number. Should start with +467x")
    @Pattern(regexp = "^\\+467[02369]\\d{7}$")
    private String mobileNumber;

    @NotBlank
    private String message;
}
