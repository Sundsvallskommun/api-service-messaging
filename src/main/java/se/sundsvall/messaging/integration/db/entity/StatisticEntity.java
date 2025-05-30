package se.sundsvall.messaging.integration.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@Entity
@Table(name = "history")
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "message_type")
	private MessageType messageType;

	@Enumerated(EnumType.STRING)
	@Column(name = "original_message_type")
	private MessageType originalMessageType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private MessageStatus status;

	@Column(name = "origin")
	private String origin;

	@Column(name = "department")
	private String department;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "municipality_id")
	private String municipalityId;
}
