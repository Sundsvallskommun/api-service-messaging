package apptest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/LetterIT/", classes = Application.class)
class LetterIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/letter";

	private static final String FIRST_NAME = "Bob";
	private static final String LAST_NAME = "Something";
	private static final String ADDRESS = "Some Street 19";
	private static final String ZIP_CODE = "123 45";
	private static final String CITY = "The City";
	private static final String COUNTRY = "The Country";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	/*
	 * PRE:
	 *  - one partyId and no address
	 *  - a single attachment with delivery type set to DIGITAL_MAIL
	 *  - successful response from digital-mail-sender
	 *
	 * POST:
	 *  - a single DIGITAL_MAIL delivery with status SENT
	 */
	@Test
	void test1_withPartyId_withoutAddress_DIGITAL_MAIL_attachment() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		assertThat(response.messages()).hasSize(1);
		assertThat(response.messages().getFirst().deliveries()).hasSize(1);

		var batchId = response.batchId();
		var messageId = response.messages().getFirst().messageId();
		var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		await()
			.atMost(10, SECONDS)
			.until(() -> {
				// Make sure that there doesn't exist a message entity
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

				// We should have a single history entry
				var history = historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
				assertThat(history).hasSize(1);

				var historyEntry = history.getFirst();
				assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
				assertThat(historyEntry.getDeliveryId()).isEqualTo(deliveryId);
				assertThat(historyEntry.getStatus()).isEqualTo(SENT);
				assertThat(historyEntry.getOriginalMessageType()).isEqualTo(LETTER);
				assertThat(historyEntry.getMessageType()).isEqualTo(DIGITAL_MAIL);
				assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
				assertThat(historyEntry.getIssuer()).isEqualTo(ISSUER);
				assertThat(historyEntry.getDestinationAddress()).isNull();
				return true;
			});
	}

	/*
	 * PRE:
	 *  - one partyId and no address
	 *  - a single attachment with delivery type set to SNAIL_MAIL
	 *  - successful response from snail-mail-sender
	 *  - successful response from citizen
	 *
	 * POST:
	 *  - a single SNAIL_MAIL delivery with status SENT
	 */
	@Test
	void test2_withPartyId_withoutAddress_SNAIL_MAIL_attachment() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		assertThat(response.messages()).hasSize(1);
		assertThat(response.messages().getFirst().deliveries()).hasSize(1);

		var batchId = response.batchId();
		var messageId = response.messages().getFirst().messageId();
		var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		await()
			.atMost(Duration.ofSeconds(10))
			.until(() -> {
				// Make sure that there doesn't exist a message entity
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

				// We should have a single history entry
				var history = historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
				assertThat(history).hasSize(1);

				var historyEntry = history.getFirst();
				assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
				assertThat(historyEntry.getDeliveryId()).isEqualTo(deliveryId);
				assertThat(historyEntry.getStatus()).isEqualTo(SENT);
				assertThat(historyEntry.getOriginalMessageType()).isEqualTo(LETTER);
				assertThat(historyEntry.getMessageType()).isEqualTo(SNAIL_MAIL);
				assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
				assertThat(historyEntry.getIssuer()).isEqualTo(ISSUER);
				assertThat(historyEntry.getDestinationAddress()).isNotNull().satisfies(destinationAddress -> {
					assertThat(destinationAddress.firstName()).isEqualTo(FIRST_NAME);
					assertThat(destinationAddress.lastName()).isEqualTo(LAST_NAME);
					assertThat(destinationAddress.address()).isEqualTo(ADDRESS);
					assertThat(destinationAddress.zipCode()).isEqualTo(ZIP_CODE);
					assertThat(destinationAddress.city()).isEqualTo(CITY);
					assertThat(destinationAddress.country()).isEqualTo(COUNTRY);
				});
				return true;
			});
	}

	/*
	 * PRE:
	 *  - one partyId and no address
	 *  - a single attachment with delivery type set to ANY
	 *  - successful response from digital-mail-sender
	 *
	 * POST:
	 *  - a single DIGITAL_MAIL delivery with status SENT
	 */
	@Test
	void test3_withPartyId_withoutAddress_ANY_attachment() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		assertThat(response.messages()).hasSize(1);
		assertThat(response.messages().getFirst().deliveries()).hasSize(1);

		var batchId = response.batchId();
		var messageId = response.messages().getFirst().messageId();
		var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		await()
			.atMost(Duration.ofSeconds(10))
			.until(() -> {
				// Make sure that there doesn't exist a message entity
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

				// We should have a single history entry
				var history = historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
				assertThat(history).hasSize(1);

				var historyEntry = history.getFirst();
				assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
				assertThat(historyEntry.getDeliveryId()).isEqualTo(deliveryId);
				assertThat(historyEntry.getStatus()).isEqualTo(SENT);
				assertThat(historyEntry.getOriginalMessageType()).isEqualTo(LETTER);
				assertThat(historyEntry.getMessageType()).isEqualTo(DIGITAL_MAIL);
				assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
				assertThat(historyEntry.getIssuer()).isEqualTo(ISSUER);
				assertThat(historyEntry.getDestinationAddress()).isNull();
				return true;
			});
	}

	/*
	 * PRE:
	 *  - one partyId and no address
	 *  - a single attachment with delivery type set to SNAIL_MAIL
	 *  - FAILURE response from citizen
	 *
	 * POST:
	 *  - a single LETTER delivery with status FAILED
	 *
	 * NOTES:
	 *  - delivery as LETTER, since we were unable to deliver as either DIGITAL_MAIL OR SNAIL_MAIL
	 */
	@Test
	void test4_withPartyId_withoutAddress_SNAIL_MAIL_attachment_and_failure_from_citizen() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		assertThat(response.messages()).hasSize(1);
		assertThat(response.messages().getFirst().deliveries()).hasSize(1);

		var batchId = response.batchId();
		var messageId = response.messages().getFirst().messageId();
		var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		await()
			.atMost(10, SECONDS)
			.until(() -> {
				// Make sure that there doesn't exist a message entity
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

				// We should have a single history entry
				var history = historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
				assertThat(history).hasSize(1);

				var historyEntry = history.getFirst();
				assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
				assertThat(historyEntry.getDeliveryId()).isEqualTo(deliveryId);
				assertThat(historyEntry.getStatus()).isEqualTo(FAILED);
				assertThat(historyEntry.getOriginalMessageType()).isEqualTo(LETTER);
				assertThat(historyEntry.getMessageType()).isEqualTo(LETTER);
				assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
				assertThat(historyEntry.getIssuer()).isEqualTo(ISSUER);
				assertThat(historyEntry.getDestinationAddress()).isNull();
				return true;
			});
	}

	/*
	 * PRE:
	 *  - no partyId and one address
	 *  - a single attachment with delivery type set to DIGITAL_MAIL
	 *
	 * POST:
	 *  - a single LETTER delivery with status FAILED
	 *
	 * NOTES:
	 *  - delivery as LETTER, since the input doesn't contain any combination of addressing and delivery mode that leads to any successful delivery
	 */
	@Test
	void test5_withoutPartyId_withAddress_DIGITAL_MAIL_attachment() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		assertThat(response.messages()).hasSize(1);
		assertThat(response.messages().getFirst().deliveries()).hasSize(1);

		var batchId = response.batchId();
		var messageId = response.messages().getFirst().messageId();
		var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		await()
			.atMost(10, SECONDS)
			.until(() -> {
				// Make sure that there doesn't exist a message entity
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

				// We should have a single history entry
				var history = historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
				assertThat(history).hasSize(1);

				var historyEntry = history.getFirst();
				assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
				assertThat(historyEntry.getDeliveryId()).isEqualTo(deliveryId);
				assertThat(historyEntry.getStatus()).isEqualTo(FAILED);
				assertThat(historyEntry.getOriginalMessageType()).isEqualTo(LETTER);
				assertThat(historyEntry.getMessageType()).isEqualTo(LETTER);
				assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
				assertThat(historyEntry.getIssuer()).isEqualTo(ISSUER);
				assertThat(historyEntry.getDestinationAddress()).isNotNull().satisfies(destinationAddress -> {
					assertThat(destinationAddress.firstName()).isEqualTo(FIRST_NAME);
					assertThat(destinationAddress.lastName()).isEqualTo(LAST_NAME);
					assertThat(destinationAddress.address()).isEqualTo(ADDRESS);
					assertThat(destinationAddress.zipCode()).isEqualTo(ZIP_CODE);
					assertThat(destinationAddress.city()).isEqualTo(CITY);
					assertThat(destinationAddress.country()).isEqualTo(COUNTRY);
				});
				return true;
			});
	}

	/*
	 * PRE:
	 *  - one partyId and one address
	 *  - a single attachment with delivery type set to ANY
	 *
	 * POST:
	 *  - one DIGITAL_MAIL delivery with status SENT
	 *  - one SNAIL_MAIL delivery with status SENT
	 */
	@Test
	void test6_withPartyId_withAddress_ANY_attachment() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		// "Extract" the messages and deliveries as a map from message id to a list of entries holding message type and status
		var messages = response.messages().stream()
			.collect(toMap(MessageResult::messageId, messageResult -> messageResult.deliveries().stream()
				.map(MessageTypeAndStatus::new).toList()));

		// We should have two messages
		assertThat(messages).hasSize(2);
		// We should have one message with one delivery and one message with two deliveries
		assertThat(messages.values()).allSatisfy(deliveries -> {
			assertThat(deliveries).hasSize(1);
			assertThat(deliveries).containsAnyOf(
				new MessageTypeAndStatus(DIGITAL_MAIL, SENT),
				new MessageTypeAndStatus(SNAIL_MAIL, SENT)
			);
		});

		// Make sure that the batch id as a proper UUID
		var batchId = response.batchId();
		assertValidUuid(batchId);

		await()
			.atMost(10, SECONDS)
			.until(() -> {
				for (var entry : messages.entrySet()) {
					var messageId = entry.getKey();
					var deliveries = entry.getValue();

					// Make sure we received message id as a proper UUID
					assertValidUuid(messageId);

					// Make sure that there doesn't exist a message entity
					assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

					// There should be as many history entries as deliveries
					var history = historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
					assertThat(history).hasSameSizeAs(deliveries);
					// Make sure that the history contains entries corresponding to the deliveries in the response
					history.forEach(historyEntry -> {
						assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
						assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
						assertThat(deliveries).contains(new MessageTypeAndStatus(historyEntry.getMessageType(), historyEntry.getStatus()));
					});
				}

				return true;
			});
	}

	/*
	 * PRE:
	 *  - one partyId and one address
	 *  - a single attachment with delivery type set to ANY
	 *  - FAILURE response from digital-mail-sender
	 *
	 * POST:
	 *  - one DIGITAL_MAIL delivery with status FAILED
	 *  - two SNAIL_MAIL deliveries with status SENT
	 */
	@Test
	void test7_withPartyId_withAddress_ANY_attachment_and_failure_from_digital_mail() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		// "Extract" the messages and deliveries as a map from message id to a list of entries holding message type and status
		var messages = response.messages().stream()
			.collect(toMap(MessageResult::messageId, messageResult -> messageResult.deliveries().stream()
				.map(MessageTypeAndStatus::new).toList()));

		// We should have two messages
		assertThat(messages).hasSize(2);
		// We should have one message with one delivery and one message with two deliveries
		assertThat(messages.values()).extracting(List::size).containsExactlyInAnyOrder(1, 2);
		assertThat(messages.values().stream().filter(list -> list.size() == 1).findFirst()).hasValueSatisfying(deliveries -> {
			assertThat(deliveries).containsExactlyInAnyOrder(new MessageTypeAndStatus(SNAIL_MAIL, SENT));
		});
		assertThat(messages.values().stream().filter(list -> list.size() == 2).findFirst()).hasValueSatisfying(deliveries -> {
			assertThat(deliveries).containsExactlyInAnyOrder(new MessageTypeAndStatus(DIGITAL_MAIL, NOT_SENT), new MessageTypeAndStatus(SNAIL_MAIL, SENT));
		});

		// Make sure that the batch id as a proper UUID
		var batchId = response.batchId();
		assertValidUuid(batchId);

		await()
			.atMost(10, SECONDS)
			.until(() -> {
				for (var entry : messages.entrySet()) {
					var messageId = entry.getKey();
					var deliveries = entry.getValue();

					// Make sure we received message id as a proper UUID
					assertValidUuid(messageId);

					// Make sure that there doesn't exist a message entity
					assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

					// There should be as many history entries as deliveries
					var history = historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
					assertThat(history).hasSameSizeAs(deliveries);
					// Make sure that the history contains entries corresponding to the deliveries in the response
					history.forEach(historyEntry -> {
						assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
						assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
						assertThat(deliveries).contains(new MessageTypeAndStatus(historyEntry.getMessageType(), historyEntry.getStatus()));
					});
				}

				return true;
			});
	}

	record MessageTypeAndStatus(MessageType messageType, MessageStatus status) {

		MessageTypeAndStatus(final DeliveryResult deliveryResult) {
			this(deliveryResult.messageType(), deliveryResult.status());
		}
	}
}
