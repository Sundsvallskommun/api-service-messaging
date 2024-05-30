package se.sundsvall.messaging.integration.snailmailsender;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class SnailMailDtoTest {

	@Test
	void testRecord() {
		var attachments = List.of(SnailMailDto.Attachment.builder().withName("name").withContentType("contentType").withContent("content").build());

		var snailMailDto = new SnailMailDto("partyId", "batchId", "department", "deviation", attachments);

		assertThat(snailMailDto.partyId()).isEqualTo("partyId");
		assertThat(snailMailDto.batchId()).isEqualTo("batchId");
		assertThat(snailMailDto.department()).isEqualTo("department");
		assertThat(snailMailDto.deviation()).isEqualTo("deviation");
		assertThat(snailMailDto.attachments()).isEqualTo(attachments);
	}
}
