package se.sundsvall.messaging.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidMessageRequestMessage;
import static se.sundsvall.messaging.TestDataFactory.createValidSlackRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSnailMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;
import static se.sundsvall.messaging.util.JsonUtils.toJson;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class MessageMapperTest {

	private final MessageMapper messageMapper = new MessageMapper();

	@Test
	void test_toMessage_withEmailRequest() {
		final var request = createValidEmailRequest();

		final var message = messageMapper.toMessage(request);

		assertThat(message.batchId()).isNull();
		assertThat(message.messageId()).isNotNull();
		assertThat(message.deliveryId()).isNotNull();
		assertThat(message.type()).isEqualTo(EMAIL);
		assertThat(message.status()).isEqualTo(PENDING);
		assertThat(message.content()).isEqualTo(toJson(request));
		assertThat(message.origin()).isEqualTo(request.origin());
	}

	@Test
	void test_toMessage_withSnailMailRequest() {
		final var request = createValidSnailMailRequest();

		final var message = messageMapper.toMessage(request);

		assertThat(message.batchId()).isNull();
		assertThat(message.messageId()).isNotNull();
		assertThat(message.deliveryId()).isNotNull();
		assertThat(message.type()).isEqualTo(SNAIL_MAIL);
		assertThat(message.status()).isEqualTo(PENDING);
		assertThat(message.content()).isEqualTo(toJson(request));
		assertThat(message.origin()).isEqualTo(request.origin());
	}

	@Test
	void test_toMessage_withSmsRequest() {
		final var request = createValidSmsRequest();

		final var message = messageMapper.toMessage(request);

		assertThat(message.messageId()).isNotNull();
		assertThat(message.deliveryId()).isNotNull();
		assertThat(message.batchId()).isNull();
		assertThat(message.type()).isEqualTo(SMS);
		assertThat(message.status()).isEqualTo(PENDING);
		assertThat(message.content()).isEqualTo(toJson(request));
		assertThat(message.origin()).isEqualTo(request.origin());
	}

	@Test
	void test_toMessage_withSmsRequestAndBatchId() {
		final var request = createValidSmsRequest();
		final var batchId = UUID.randomUUID().toString();

		final var message = messageMapper.toMessage(request, batchId);

		assertThat(message.messageId()).isNotNull();
		assertThat(message.deliveryId()).isNotNull();
		assertThat(message.batchId()).isEqualTo(batchId);
		assertThat(message.type()).isEqualTo(SMS);
		assertThat(message.status()).isEqualTo(PENDING);
		assertThat(message.content()).isEqualTo(toJson(request));
		assertThat(message.origin()).isEqualTo(request.origin());
	}

	@Test
	void test_toMessage_withWebMessageRequest() {
		final var request = createValidWebMessageRequest();

		final var message = messageMapper.toMessage(request);

		assertThat(message.batchId()).isNull();
		assertThat(message.messageId()).isNotNull();
		assertThat(message.type()).isEqualTo(WEB_MESSAGE);
		assertThat(message.status()).isEqualTo(PENDING);
		assertThat(message.content()).isEqualTo(toJson(request));
		assertThat(message.origin()).isEqualTo(request.origin());
	}

	@Test
	void test_toMessages_withDigitalMailRequest() {
		final var request = createValidDigitalMailRequest();

		final var messages = messageMapper.toMessages(request, "someBatchId");

		assertThat(messages).hasSize(1);

		final var message = messages.getFirst();

		assertThat(message.batchId()).isEqualTo("someBatchId");
		assertThat(message.messageId()).isNotNull();
		assertThat(message.type()).isEqualTo(DIGITAL_MAIL);
		assertThat(message.status()).isEqualTo(PENDING);
		assertThat(message.content()).isEqualTo(toJson(request));
		assertThat(message.origin()).isEqualTo(request.origin());
	}

	@Test
	void test_toMessage_withBatchIdAndMessageRequest() {
		final var batchId = UUID.randomUUID().toString();

		final var origin = "someOrigin";
		final var issuer = "someIssuer";
		final var municipalityId = "someMunicipalityId";
		final var request = createValidMessageRequestMessage();

		final var message = messageMapper.toMessage(municipalityId, origin, issuer, batchId, request);

		assertThat(message.batchId()).isEqualTo(batchId);
		assertThat(message.messageId()).isNotNull();
		assertThat(message.type()).isEqualTo(MESSAGE);
		assertThat(message.status()).isEqualTo(PENDING);
		assertThat(message.content()).isEqualTo(toJson(request));
		assertThat(message.origin()).isEqualTo(origin);
		assertThat(message.issuer()).isEqualTo(issuer);
	}

	@Test
	void test_toMessage_withSlackRequest() {
		final var request = createValidSlackRequest();

		final var message = messageMapper.toMessage(request);

		assertThat(message.messageId()).isNotNull();
		assertThat(message.deliveryId()).isNotNull();
		assertThat(message.batchId()).isNull();
		assertThat(message.type()).isEqualTo(SLACK);
		assertThat(message.status()).isEqualTo(PENDING);
		assertThat(message.content()).isEqualTo(toJson(request));
		assertThat(message.origin()).isEqualTo(request.origin());
	}
}
