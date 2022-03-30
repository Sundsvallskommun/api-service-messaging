package se.sundsvall.messaging.api.model;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

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
    private Sender.Sms sender;

    @NotBlank
    @Schema(required = true, description = "Mobile number. Should start with +467x")
    @Pattern(regexp = "^\\+467[02369]\\d{7}$")
    private String mobileNumber;

    @NotBlank
    private String message;
}
