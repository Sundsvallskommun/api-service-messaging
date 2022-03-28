package se.sundsvall.messaging.integration.db.entity;

import java.time.LocalDateTime;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
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
import lombok.Setter;

@Entity
@Table(name = "messages")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder(setterPrefix = "with", toBuilder = true)
@Getter
@Setter
public class MessageEntity {

    @Id
    @Column(name = "message_id")
    private String messageId;

    @Column(name = "barch_id")
    private String batchId;

    @Column(name = "party_id")
    private String partyId;

    @Column(name = "email_name")
    private String emailName;

    @Column(name = "sms_name")
    private String smsName;

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(name = "subject")
    private String subject;

    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_status")
    private MessageStatus messageStatus;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "messages_external_references", joinColumns = @JoinColumn(name = "message_id"))
    private Map<String, String> externalReferences;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Generated  // Dirty "fix", since somehow leaving this out f*cks up the Jacoco coverage...
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
