package se.sundsvall.messaging.api.model.response;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

@Builder(setterPrefix = "with")
@Schema(description = "User messages model")
public record UserMessages(

	@JsonProperty("_meta") @Schema(implementation = PagingMetaData.class, accessMode = READ_ONLY) PagingMetaData metaData,

	@ArraySchema(schema = @Schema(implementation = UserMessage.class, accessMode = READ_ONLY)) List<UserMessage> messages

) {
}
