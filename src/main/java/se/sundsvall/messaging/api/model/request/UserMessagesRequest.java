package se.sundsvall.messaging.api.model.request;

import jakarta.validation.constraints.NotBlank;

import se.sundsvall.dept44.models.api.paging.AbstractParameterPagingBase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "User messages request")
@Builder(setterPrefix = "with")
@Getter
@Setter
public class UserMessagesRequest extends AbstractParameterPagingBase {

	@Schema(description = "The user identifier", example = "and06sod")
	@NotBlank
	private String userId;

}
