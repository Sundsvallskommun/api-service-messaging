package se.sundsvall.messaging.integration.snailmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createAddress;

import generated.se.sundsvall.snailmail.Attachment;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SnailMailSenderIntegrationMapperTest {

	private final SnailMailSenderIntegrationMapper mapper = new SnailMailSenderIntegrationMapper();

	@Test
	void test_toSendSnailmailRequest_whenRequestIsNull() {
		assertThat(mapper.toSendSnailmailRequest(null)).isNull();
	}

	@Test
	void test_toSendSnailmailRequest() {
		var dto = SnailMailDto.builder()
			.withAttachments(List.of(SnailMailDto.Attachment.builder()
				.withName("someName")
				.withContentType(Attachment.ContentTypeEnum.APPLICATION_PDF.getValue())
				.withContent("someContent")
				.build()))
			.build();

		var mappedRequest = mapper.toSendSnailmailRequest(dto);

		assertThat(mappedRequest.getAttachments()).hasSize(1);
	}

	@Test
	void test_toAddress() {
		var address = createAddress();

		var snailMailAddress = mapper.toAddress(address);

		assertThat(snailMailAddress.getAddress()).isEqualTo(address.address());
		assertThat(snailMailAddress.getCity()).isEqualTo(address.city());
		assertThat(snailMailAddress.getCountry()).isEqualTo(address.country());
		assertThat(snailMailAddress.getZipCode()).isEqualTo(address.zipCode());
		assertThat(snailMailAddress.getFirstName()).isEqualTo(address.firstName());
		assertThat(snailMailAddress.getLastName()).isEqualTo(address.lastName());
		assertThat(snailMailAddress.getApartmentNumber()).isEqualTo(address.apartmentNumber());
	}
}
