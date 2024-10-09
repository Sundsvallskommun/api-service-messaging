package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class MessageTest {
	private static final String BATCH_ID = "batchId";
	private static final String MESSAGE_ID = "messageId";
	private static final String DELIVERY_ID = "deliveryId";
	private static final String PARTY_ID = "partyId";
	private static final MessageType TYPE = MessageType.DIGITAL_INVOICE;
	private static final MessageType ORIGINAL_TYPE = MessageType.SMS;
	private static final MessageStatus STATUS = MessageStatus.NO_CONTACT_SETTINGS_FOUND;
	private static final String CONTENT = "content";
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final String MUNICIPALITY_ID = "municipalityId";

	@Test
	void testConstructor() {
		final var bean = new Message(BATCH_ID, MESSAGE_ID, DELIVERY_ID, PARTY_ID, TYPE, ORIGINAL_TYPE, STATUS, CONTENT, ORIGIN, ISSUER, MUNICIPALITY_ID);

		assertBean(bean);
	}

	@Test
	void testBuilder() {
		final var bean = Message.builder()
			.withBatchId(BATCH_ID)
			.withContent(CONTENT)
			.withDeliveryId(DELIVERY_ID)
			.withIssuer(ISSUER)
			.withMessageId(MESSAGE_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withOrigin(ORIGIN)
			.withOriginalType(ORIGINAL_TYPE)
			.withPartyId(PARTY_ID)
			.withStatus(STATUS)
			.withType(TYPE)
			.build();

		assertBean(bean);
	}

	private void assertBean(final Message bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.batchId()).isEqualTo(BATCH_ID);
		assertThat(bean.content()).isEqualTo(CONTENT);
		assertThat(bean.deliveryId()).isEqualTo(DELIVERY_ID);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.messageId()).isEqualTo(MESSAGE_ID);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.originalType()).isEqualTo(ORIGINAL_TYPE);
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
		assertThat(bean.status()).isEqualTo(STATUS);
		assertThat(bean.type()).isEqualTo(TYPE);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Message.builder().build()).hasAllNullFieldsOrProperties();
	}
}
