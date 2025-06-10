package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.response.Batch.Status;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class BatchTest {

	private static final String BATCH_ID = "batchId";
	private static final LocalDateTime SENT = LocalDateTime.now();
	private static final String MESSAGE_TYPE = "messageType";
	private static final String SUBJECT = "subject";
	private static final int ATTACHMENT_COUNT = 11;
	private static final int RECIPIENT_COUNT = 22;
	private static final int SUCCESSFULL = 33;
	private static final int UNSUCCESSFUL = 44;

	@Test
	void batchConstructor() {
		final var bean = new Batch(BATCH_ID, MESSAGE_TYPE, SUBJECT, SENT, ATTACHMENT_COUNT, RECIPIENT_COUNT, new Status(SUCCESSFULL, UNSUCCESSFUL));

		assertBatchValues(bean);
	}

	@Test
	void batchBuilder() {
		final var bean = Batch.builder()
			.withAttachmentCount(ATTACHMENT_COUNT)
			.withBatchId(BATCH_ID)
			.withMessageType(MESSAGE_TYPE)
			.withRecipientCount(RECIPIENT_COUNT)
			.withSent(SENT)
			.withStatus(Status.builder()
				.withSuccessful(SUCCESSFULL)
				.withUnsuccessful(UNSUCCESSFUL)
				.build())
			.withSubject(SUBJECT)
			.build();

		assertBatchValues(bean);
	}

	private void assertBatchValues(Batch bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.attachmentCount()).isEqualTo(ATTACHMENT_COUNT);
		assertThat(bean.batchId()).isEqualTo(BATCH_ID);
		assertThat(bean.messageType()).isEqualTo(MESSAGE_TYPE);
		assertThat(bean.recipientCount()).isEqualTo(RECIPIENT_COUNT);
		assertThat(bean.sent()).isEqualTo(SENT);
		assertThat(bean.status().successful()).isEqualTo(SUCCESSFULL);
		assertThat(bean.status().unsuccessful()).isEqualTo(UNSUCCESSFUL);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
		assertThat(bean).hasOnlyFields("attachmentCount", "batchId", "messageType", "recipientCount", "sent", "status", "subject");
	}

	@Test
	void batchStatusConstructor() {
		final var bean = new Status(SUCCESSFULL, UNSUCCESSFUL);

		assertStatusValues(bean);
	}

	@Test
	void batchStatusBuilder() {
		final var bean = Status.builder()
			.withSuccessful(SUCCESSFULL)
			.withUnsuccessful(UNSUCCESSFUL)
			.build();

		assertStatusValues(bean);
	}

	private void assertStatusValues(Status bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.successful()).isEqualTo(SUCCESSFULL);
		assertThat(bean.unsuccessful()).isEqualTo(UNSUCCESSFUL);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(Batch.builder().build()).hasAllNullFieldsOrPropertiesExcept("attachmentCount", "recipientCount")
			.hasFieldOrPropertyWithValue("attachmentCount", 0)
			.hasFieldOrPropertyWithValue("recipientCount", 0);
		assertThat(new Batch(null, null, null, null, 0, 0, null))
			.hasAllNullFieldsOrPropertiesExcept("attachmentCount", "recipientCount")
			.hasFieldOrPropertyWithValue("attachmentCount", 0)
			.hasFieldOrPropertyWithValue("recipientCount", 0);

		assertThat(Status.builder().build()).hasAllNullFieldsOrPropertiesExcept("successful", "unsuccessful")
			.hasFieldOrPropertyWithValue("successful", 0)
			.hasFieldOrPropertyWithValue("unsuccessful", 0);
		assertThat(new Status(0, 0)).hasAllNullFieldsOrPropertiesExcept("successful", "unsuccessful")
			.hasFieldOrPropertyWithValue("successful", 0)
			.hasFieldOrPropertyWithValue("unsuccessful", 0);
	}
}
