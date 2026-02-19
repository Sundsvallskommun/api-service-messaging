package se.sundsvall.messaging.integration.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import static se.sundsvall.messaging.util.JsonUtils.fromJson;
import static se.sundsvall.messaging.util.JsonUtils.toJson;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "message_id")
	private String messageId;

	@Column(name = "batch_id")
	private String batchId;

	@With
	@Column(name = "delivery_id")
	private String deliveryId;

	@Column(name = "party_id")
	private String partyId;

	@With
	@Enumerated(EnumType.STRING)
	@Column(name = "message_type")
	private MessageType type;

	@With
	@Enumerated(EnumType.STRING)
	@Column(name = "original_message_type")
	private MessageType originalMessageType;

	@With
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private MessageStatus status;

	@With
	@Column(name = "content", columnDefinition = "LONGTEXT NOT NULL")
	private String content;

	@With
	@Column(name = "origin")
	private String origin;

	@With
	@Column(name = "issuer")
	private String issuer;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "municipality_id")
	private String municipalityId;

	@With
	@Transient
	private Address destinationAddress;

	@Column(name = "destination_address")
	private String destinationAddressJson;

	@Column(name = "organizationNumber", length = 12)
	private String organizationNumber;

	@Builder(setterPrefix = "with")
	private MessageEntity(final Long id, final String messageId, final String batchId, final String deliveryId, final String partyId, final MessageType type, final MessageType originalMessageType, final MessageStatus status, final String content,
		final String origin, final String issuer, final LocalDateTime createdAt,
		final String municipalityId, final Address destinationAddress, final String organizationNumber) {
		this.id = id;
		this.messageId = messageId;
		this.batchId = batchId;
		this.deliveryId = deliveryId;
		this.partyId = partyId;
		this.type = type;
		this.originalMessageType = originalMessageType;
		this.status = status;
		this.content = content;
		this.origin = origin;
		this.issuer = issuer;
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
