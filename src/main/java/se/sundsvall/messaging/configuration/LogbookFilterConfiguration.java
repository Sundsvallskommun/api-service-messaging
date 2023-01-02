package se.sundsvall.messaging.configuration;

import static org.zalando.logbook.BodyFilter.merge;
import static org.zalando.logbook.BodyFilters.defaultValue;

import java.util.Collection;
import java.util.Map;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.BodyFilter;

@Configuration
class LogbookFilterConfiguration {

    private final com.jayway.jsonpath.Configuration jsonPathConfiguration;

    private final Map<String, String> jsonPathFilters;

    LogbookFilterConfiguration(
            @Value("#{${logbook.json-path-exclusion-filters: {} }}")
            final Map<String, String> jsonPathFilters) {
        this.jsonPathFilters = jsonPathFilters;

        jsonPathConfiguration = com.jayway.jsonpath.Configuration.builder()
            .jsonProvider(new JacksonJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .options(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST)
            .build();
    }

    @Bean
    BodyFilter bodyFilter() {
        return merge(defaultValue(), (contentType, body) -> {
            if ("".equals(body.trim())) {
                return body;
            }

            var documentContext = JsonPath.using(jsonPathConfiguration).parse(body);
            jsonPathFilters.forEach((key, value) -> replace(documentContext, key, value));
            return documentContext.jsonString();
        });
    }

    static void replace(final DocumentContext documentContext, final String jsonPath, final String replacement) {
        var value = documentContext.read(jsonPath);

        if (value instanceof Collection<?> valueAsCollection && !valueAsCollection.isEmpty()) {
            documentContext.set(jsonPath, replacement);
        }
    }
}
