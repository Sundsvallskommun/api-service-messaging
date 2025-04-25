package se.sundsvall.messaging.integration.oepintegrator;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static se.sundsvall.messaging.integration.oepintegrator.OepIntegratorIntegration.INTEGRATION_NAME;

import generated.se.sundsvall.oepintegrator.WebmessageRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.oep-integrator.base-url}",
	configuration = OepIntegratorConfiguration.class)
@CircuitBreaker(name = INTEGRATION_NAME)
interface OepIntegratorClient {

	@PostMapping(path = "/{municipalityId}/{instanceType}/webmessages", consumes = MULTIPART_FORM_DATA_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> createWebmessage(
		@PathVariable(value = "municipalityId") final String municipalityId,
		@PathVariable(value = "instanceType") final String instanceType,
		@Valid @RequestPart(value = "request") final WebmessageRequest request,
		@RequestPart(value = "attachments", required = false) final List<AttachmentMultipartFile> attachments);
}
