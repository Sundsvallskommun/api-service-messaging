package se.sundsvall.messaging.integration.snailmailsender;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.Address;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createAddress;

class SnailMailDtoTest {

	private static final Address ADDRESS = createAddress();

	@Test
	void testRecord() {
		var attachments = List.of(SnailMailDto.Attachment.builder().withName("name").withContentType("contentType").withContent("content").build());

		var snailMailDto = new SnailMailDto("partyId", ADDRESS, "batchId", "department", "deviation", attachments);

		assertThat(snailMailDto.partyId()).isEqualTo("partyId");
		assertThat(snailMailDto.batchId()).isEqualTo("batchId");
		assertThat(snailMailDto.department()).isEqualTo("department");
		assertThat(snailMailDto.deviation()).isEqualTo("deviation");
		assertThat(snailMailDto.attachments()).isEqualTo(attachments);
	}
}
