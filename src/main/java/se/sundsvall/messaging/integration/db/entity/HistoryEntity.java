package se.sundsvall.messaging.integration.db.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
	@Index(name = "idx_history_municipality_id", columnList = "municipality_id")
})
@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

	@Generated // Dirty "fix", since somehow leaving this out f*cks up the Jacoco coverage...
	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}

}
