package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class HistoryResponseTest {

	@Test
	void testBuilderAndGetters() {
		final var historyResponse = HistoryResponse.builder()
			.withMessageType(WEB_MESSAGE)
			.withStatus(SENT)
			.withContent("someContent")
			.withTimestamp(LocalDateTime.now())
			.build();

		assertThat(historyResponse).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(historyResponse.messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(historyResponse.status()).isEqualTo(SENT);
		assertThat(historyResponse.content()).isEqualTo("someContent");
		assertThat(historyResponse.timestamp()).isNotNull();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(HistoryResponse.builder().build()).hasAllNullFieldsOrProperties();
	}
}
