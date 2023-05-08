package se.sundsvall.messaging.model;

import java.io.IOException;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import se.sundsvall.messaging.api.model.request.validation.ValidHeaderName;

import generated.se.sundsvall.messagingrules.HeaderName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record Header(

    @ValidHeaderName
    @Schema(description = "The header name")
    @JsonDeserialize(using = HeaderNameDeserializer.class)
    HeaderName name,

    @NotEmpty
    @Schema(description = "The header values")
    List<String> values) {

    /**
     * Custom Jackson deserializer used to avoid JSON parsing exceptions being thrown if invalid
     * header names are passed into the service.
     */
    public static class HeaderNameDeserializer extends StdDeserializer<HeaderName> {

        public HeaderNameDeserializer() {
            super((Class<?>) null);
        }

        @Override
        public HeaderName deserialize(final JsonParser jsonParser, final DeserializationContext context)
            throws IOException {
            var node = (JsonNode) jsonParser.getCodec().readTree(jsonParser);

            for (var headerName : HeaderName.values()) {
                if (headerName.name().equals(node.asText())) {
                    return headerName;
                }
            }

            return null;
        }
    }
}
