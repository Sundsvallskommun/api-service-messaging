package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class HistoryTest {

	private static final String BATCH_ID = "batchId";
	private static final String MESSAGE_ID = "messageId";
	private static final String DELIVERY_ID = "deliveryId";
	private static final MessageType MESSAGE_TYPE = MessageType.SNAIL_MAIL;
	private static final MessageType ORIGINAL_MESSAGE_TYPE = MessageType.DIGITAL_MAIL;
	private static final MessageStatus STATUS = MessageStatus.FAILED;
	private static final String CONTENT = "content";
	private static final LocalDateTime CREATED_AT = LocalDateTime.now();
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";

	@Test
	void testConstructor() {
		final var bean = new History(BATCH_ID, MESSAGE_ID, DELIVERY_ID, MESSAGE_TYPE, ORIGINAL_MESSAGE_TYPE, STATUS, CONTENT, ORIGIN, ISSUER, CREATED_AT, MUNICIPALITY_ID);

		assertBean(bean);
	}

	@Test
	void testBuilder() {
		final var bean = History.builder()
			.withBatchId(BATCH_ID)
			.withContent(CONTENT)
			.withCreatedAt(CREATED_AT)
			.withDeliveryId(DELIVERY_ID)
			.withIssuer(ISSUER)
			.withMessageId(MESSAGE_ID)
			.withMessageType(MESSAGE_TYPE)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withOrigin(ORIGIN)
			.withOriginalMessageType(ORIGINAL_MESSAGE_TYPE)
			.withStatus(STATUS)
			.build();

		assertBean(bean);
	}

	private void assertBean(final History bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.batchId()).isEqualTo(BATCH_ID);
		assertThat(bean.messageId()).isEqualTo(MESSAGE_ID);
		assertThat(bean.deliveryId()).isEqualTo(DELIVERY_ID);
		assertThat(bean.messageType()).isEqualTo(MESSAGE_TYPE);
		assertThat(bean.originalMessageType()).isEqualTo(ORIGINAL_MESSAGE_TYPE);
		assertThat(bean.status()).isEqualTo(STATUS);
		assertThat(bean.content()).isEqualTo(CONTENT);
		assertThat(bean.createdAt()).isEqualTo(CREATED_AT);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(History.builder().build()).hasAllNullFieldsOrProperties();
	}
}
