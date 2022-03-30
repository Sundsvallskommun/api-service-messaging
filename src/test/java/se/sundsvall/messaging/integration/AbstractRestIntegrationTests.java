package se.sundsvall.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AbstractRestIntegrationTests {

    private final AbstractRestIntegration integration = new AbstractRestIntegration() { };

    @Test
    void testCreateRequestEntityAndHeaders() {
        var body = "test body";

        var requestEntity = integration.createRequestEntity(body);

        assertThat(requestEntity.hasBody()).isTrue();
        assertThat(requestEntity.getBody()).isEqualTo(body);
        assertThat(requestEntity.getHeaders()).containsKeys("Accept", "Content-Type");
    }
}
