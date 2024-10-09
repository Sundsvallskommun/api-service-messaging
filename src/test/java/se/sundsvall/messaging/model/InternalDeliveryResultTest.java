package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.EMAIL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InternalDeliveryResultTest {

	private static final String MESSAGE_ID = "messageId";
	private static final String DELIVERY_ID = "deliveryId";
	private static final String MUNICIPALITY_ID = "municipalityId";

	@Mock
	private Message mockMessage;

	@Test
	void testDefaultConstructor() {
		final var bean = new InternalDeliveryResult(MESSAGE_ID, DELIVERY_ID, EMAIL, SENT, MUNICIPALITY_ID);

		assertBean(bean, SENT);
	}

	@Test
	void testBuilder() {
		final var bean = InternalDeliveryResult.builder()
			.withDeliveryId(DELIVERY_ID)
			.withMessageId(MESSAGE_ID)
			.withMessageType(EMAIL)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withStatus(FAILED)
			.build();

		assertBean(bean, FAILED);
	}

	@Test
	void testConstructorAcceptingMessageEntity() {
		when(mockMessage.messageId()).thenReturn(MESSAGE_ID);
		when(mockMessage.deliveryId()).thenReturn(DELIVERY_ID);
		when(mockMessage.type()).thenReturn(EMAIL);
		when(mockMessage.municipalityId()).thenReturn(MUNICIPALITY_ID);
		when(mockMessage.status()).thenReturn(PENDING);

		final var bean = new InternalDeliveryResult(mockMessage);

		assertBean(bean, PENDING);

		verify(mockMessage).messageId();
		verify(mockMessage).deliveryId();
		verify(mockMessage).status();
		verify(mockMessage).municipalityId();
		verify(mockMessage).type();
		verifyNoMoreInteractions(mockMessage);
	}

	private void assertBean(final InternalDeliveryResult bean, MessageStatus expectedStatus) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.messageId()).isEqualTo(MESSAGE_ID);
		assertThat(bean.deliveryId()).isEqualTo(DELIVERY_ID);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(bean.messageType()).isEqualTo(EMAIL);
		assertThat(bean.status()).isEqualTo(expectedStatus);
	}

	@Test
	void testConstructorAcceptingMessageEntityAndStatus() {
		when(mockMessage.messageId()).thenReturn(MESSAGE_ID);
		when(mockMessage.deliveryId()).thenReturn(DELIVERY_ID);
		when(mockMessage.type()).thenReturn(EMAIL);
		when(mockMessage.municipalityId()).thenReturn(MUNICIPALITY_ID);

		final var bean = new InternalDeliveryResult(mockMessage, SENT);

		assertBean(bean, SENT);

		verify(mockMessage).messageId();
		verify(mockMessage).deliveryId();
		verify(mockMessage).municipalityId();
		verify(mockMessage).type();
		verifyNoMoreInteractions(mockMessage);
	}

	@Test
	void testConstructorAcceptingMessageId() {
		final var bean = new InternalDeliveryResult(MESSAGE_ID);

		assertThat(bean).isNotNull().hasAllNullFieldsOrPropertiesExcept("messageId");
		assertThat(bean.messageId()).isEqualTo(MESSAGE_ID);
	}

	@Test
	void testConstructorAcceptingAllButStatus() {
		final var bean = new InternalDeliveryResult(MESSAGE_ID, DELIVERY_ID, EMAIL, MUNICIPALITY_ID);

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("status");
		assertThat(bean.messageId()).isEqualTo(MESSAGE_ID);
		assertThat(bean.deliveryId()).isEqualTo(DELIVERY_ID);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(bean.messageType()).isEqualTo(EMAIL);
		assertThat(bean.status()).isNull();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(InternalDeliveryResult.builder().build()).hasAllNullFieldsOrProperties();
	}

}
