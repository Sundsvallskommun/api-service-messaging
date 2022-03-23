package se.sundsvall.messaging.api.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
@Getter
@Setter
@ToString
@Schema(name = "IncomingSmsRequest", description = "SMS representation")
public class IncomingSmsRequest {

    @NotBlank
    @Size(max = 11)
    @Schema(required = true, description = "The Sender of the SMS", maxLength = 11, example = "sender")
    private String sender;

    @Schema(required = true, description = "party ID", example = "cb88e940-bbb4-470d-8908-826021945e4f")
    private String partyId;

    @NotBlank
    @Schema(required = true, description = "Mobile number should start with +467x")
    @Pattern(regexp = "^\\+467[02369]\\d{7}$")
    private String mobileNumber;

    @NotBlank
    private String message;
}
