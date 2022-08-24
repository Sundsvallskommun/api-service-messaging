package se.sundsvall.messaging.model;

import java.io.IOException;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import se.sundsvall.messaging.api.model.validation.ValidHeaderName;

import generated.se.sundsvall.businessrules.HeaderName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Header {

    @ValidHeaderName
    @Schema(description = "The header name")
    @JsonDeserialize(using = HeaderNameDeserializer.class)
    private HeaderName name;

    @NotEmpty
    @Schema(description = "The header values")
    private List<String> values;

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
