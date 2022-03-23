package se.sundsvall.messaging.integration;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class AbstractRestIntegration {

    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
