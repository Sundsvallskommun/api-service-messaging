package se.sundsvall.messaging.integration.db.entity;

import static se.sundsvall.messaging.util.JsonUtils.fromJson;
import static se.sundsvall.messaging.util.JsonUtils.toJson;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@Entity
@Table(name = "history", indexes = {
	@Index(name = "idx_history_batch_id", columnList = "batch_id"),
	@Index(name = "idx_history_message_id", columnList = "message_id"),
	@Index(name = "idx_history_delivery_id", columnList = "delivery_id"),
	@Index(name = "idx_history_origin", columnList = "origin"),
	@Index(name = "idx_history_issuer", columnList = "issuer"),
	@Index(name = "idx_history_department", columnList = "department"),
	@Index(name = "idx_history_municipality_id", columnList = "municipality_id"),
	@Index(name = "idx_history_organization_number", columnList = "organization_number")
})
@Getter
@NoArgsConstructor
public class HistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "batch_id", length = 36)
	private String batchId;

	@Column(name = "message_id", nullable = false, length = 36)
	private String messageId;

	@Column(name = "delivery_id", length = 36)
	private String deliveryId;

	@Column(name = "party_id", length = 36)
	private String partyId;

	@Enumerated(EnumType.STRING)
	@Column(name = "message_type")
	private MessageType messageType;

	@Enumerated(EnumType.STRING)
	@Column(name = "original_message_type")
	private MessageType originalMessageType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private MessageStatus status;

	@Column(name = "status_detail", columnDefinition = "LONGTEXT")
	private String statusDetail;

	@Column(name = "content", columnDefinition = "LONGTEXT")
	private String content;

	@Column(name = "origin")
	private String origin;

	@Column(name = "issuer")
	private String issuer;

	@Column(name = "department")
	private String department;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Transient
	private Address destinationAddress;

	@Column(name = "destination_address")
	private String destinationAddressJson;

	@Column(name = "organization_number", length = 12)
	private String organizationNumber;

	@Builder(setterPrefix = "with")
	private HistoryEntity(final Long id, final String batchId, final String messageId, final String deliveryId, final String partyId,
		final MessageType messageType, final MessageType originalMessageType, final MessageStatus status, final String statusDetail,
		final String content, final String origin, final String issuer, final String department, final LocalDateTime createdAt,
		final String municipalityId, final Address destinationAddress, final String organizationNumber) {
		this.id = id;
		this.batchId = batchId;
		this.messageId = messageId;
		this.deliveryId = deliveryId;
		this.partyId = partyId;
		this.messageType = messageType;
		this.originalMessageType = originalMessageType;
		this.status = status;
		this.statusDetail = statusDetail;
		this.content = content;
		this.origin = origin;
		this.issuer = issuer;
		this.department = department;
		this.createdAt = createdAt;
		this.municipalityId = municipalityId;
		this.destinationAddress = destinationAddress;
		this.organizationNumber = organizationNumber;
	}

	String getDestinationAddressJson() {
		return destinationAddressJson;
	}

	void setDestinationAddressJson(final String destinationAddressJson) {
		this.destinationAddressJson = destinationAddressJson;
	}

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();

		destinationAddressJson = toJson(destinationAddress);
	}

	@PostLoad
	void postLoad() {
		destinationAddress = fromJson(destinationAddressJson, Address.class);
	}
}
