package se.sundsvall.messaging.integration.objectstore;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static se.sundsvall.messaging.integration.objectstore.ObjectStoreIntegration.INTEGRATION_NAME;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${integration.object-store.base-url}",
	configuration = ObjectStoreIntegrationConfiguration.class)
@CircuitBreaker(name = INTEGRATION_NAME)
interface ObjectStoreClient {

	/**
	 * Reads an object's bytes. The response is taken as a whole entity rather than a bare byte array because the store
	 * replays the content type and file name it was given on store as headers, and those are the fallback for what an
	 * attachment is called and what it claims to be when the caller did not say.
	 */
	@GetMapping("/objects/{bucket}/{objectId}")
	ResponseEntity<byte[]> readObject(@PathVariable final String bucket, @PathVariable final String objectId);

}
