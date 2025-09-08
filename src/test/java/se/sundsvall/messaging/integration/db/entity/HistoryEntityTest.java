package se.sundsvall.messaging.integration.db.entity;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

class HistoryEntityTest {

	@Test
	void testBuilder() {
		var id = 1234L;
		var batchId = "batchId";
		var messageId = "messageId";
		var deliveryId = "deliveryId";
		var partyId = "partyId";
		var messageType = MessageType.SLACK;
		var originalMessageType = MessageType.EMAIL;
		var status = MessageStatus.NO_CONTACT_WANTED;
		var statusDetail = "statusDetail";
		var content = "content";
		var origin = "origin";
		var issuer = "issuer";
		var department = "department";
		var createdAt = LocalDateTime.now();
		var municipalityId = "municipalityId";
		var destinationAddress = Address.builder().withAddress("someStreet").build();
		var organizationNumber = "1234567890";

		var bean = HistoryEntity.builder()
			.withBatchId(batchId)
			.withContent(content)
			.withCreatedAt(createdAt)
			.withDeliveryId(deliveryId)
			.withDepartment(department)
			.withId(id)
			.withIssuer(issuer)
			.withMessageId(messageId)
			.withMessageType(messageType)
			.withMunicipalityId(municipalityId)
			.withOrigin(origin)
			.withOriginalMessageType(originalMessageType)
			.withPartyId(partyId)
			.withStatus(status)
			.withStatusDetail(statusDetail)
			.withDestinationAddress(destinationAddress)
			.withOrganizationNumber(organizationNumber)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("destinationAddressJson");
		assertThat(bean.getBatchId()).isEqualTo(batchId);
		assertThat(bean.getContent()).isEqualTo(content);
		assertThat(bean.getCreatedAt()).isEqualTo(createdAt);
		assertThat(bean.getDeliveryId()).isEqualTo(deliveryId);
		assertThat(bean.getDepartment()).isEqualTo(department);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getIssuer()).isEqualTo(issuer);
		assertThat(bean.getMessageId()).isEqualTo(messageId);
		assertThat(bean.getMessageType()).isEqualTo(messageType);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getOrigin()).isEqualTo(origin);
		assertThat(bean.getOriginalMessageType()).isEqualTo(originalMessageType);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getStatusDetail()).isEqualTo(statusDetail);
		assertThat(bean.getDestinationAddress()).isEqualTo(destinationAddress);
		assertThat(bean.getOrganizationNumber()).isEqualTo(organizationNumber);
	}

	@Test
	void noDirtOnCreatedBean() {
		assertThat(HistoryEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("additionalMetadata");
		assertThat(new HistoryEntity()).hasAllNullFieldsOrPropertiesExcept("additionalMetadata");
	}

	@Test
	void testPrePersist() {
		var entity = HistoryEntity.builder()
			.withDestinationAddress(Address.builder().withAddress("someAddress").build())
			.build();
		assertThat(entity.getCreatedAt()).isNull();
		assertThat(entity.getDestinationAddressJson()).isNull();

		entity.prePersist();

		assertThat(entity.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
		assertThat(entity.getDestinationAddressJson()).isEqualTo("{\"address\":\"someAddress\"}");
	}

	@Test
	void testPostLoad() {
		var entity = new HistoryEntity();
		entity.setDestinationAddressJson("{\"address\":\"someAddress\"}");

		assertThat(entity.getDestinationAddress()).isNull();

		entity.postLoad();

		assertThat(entity.getDestinationAddress()).isNotNull();
		assertThat(entity.getDestinationAddress().address()).isEqualTo("someAddress");
	}
}
