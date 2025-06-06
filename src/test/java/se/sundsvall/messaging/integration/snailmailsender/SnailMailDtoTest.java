package se.sundsvall.messaging.integration.snailmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createAddress;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.Address;

class SnailMailDtoTest {

	private static final Address ADDRESS = createAddress();

	@Test
	void testRecord() {
		var attachments = List.of(SnailMailDto.Attachment.builder().withFilename("name").withContentType("contentType").withContent("content").build());

		var snailMailDto = new SnailMailDto("partyId", ADDRESS, "batchId", "department", "deviation", "sentBy", "origin", attachments);

		assertThat(snailMailDto.partyId()).isEqualTo("partyId");
		assertThat(snailMailDto.batchId()).isEqualTo("batchId");
		assertThat(snailMailDto.department()).isEqualTo("department");
		assertThat(snailMailDto.deviation()).isEqualTo("deviation");
		assertThat(snailMailDto.sentBy()).isEqualTo("sentBy");
		assertThat(snailMailDto.origin()).isEqualTo("origin");
		assertThat(snailMailDto.attachments()).isEqualTo(attachments);
	}
}
