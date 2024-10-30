package se.sundsvall.messaging.api.model.response;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with")
@Schema(description = "User messages model")
public record UserMessages(

	@JsonProperty("_meta") @Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY) PagingMetaData metaData,

	@ArraySchema(schema = @Schema(implementation = UserMessage.class, accessMode = READ_ONLY)) List<UserMessage> messages

) {
}
