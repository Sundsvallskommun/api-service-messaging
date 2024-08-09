package se.sundsvall.messaging.integration.db.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

@Entity
@Table(name = "messages")
@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
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

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Generated  // Dirty "fix", since somehow leaving this out f*cks up the Jacoco coverage...
	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}

}
