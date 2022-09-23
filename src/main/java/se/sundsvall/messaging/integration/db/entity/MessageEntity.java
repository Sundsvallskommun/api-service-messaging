package se.sundsvall.messaging.integration.db.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder(setterPrefix = "with")
@Getter
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
    @Column(name = "status")
    private MessageStatus status;

    @With
    @Column(name = "content", columnDefinition = "LONGTEXT NOT NULL")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Generated  // Dirty "fix", since somehow leaving this out f*cks up the Jacoco coverage...
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
