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
import javax.persistence.Lob;
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
@Table(name = "history")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
@Getter
@Setter
public class HistoryEntity {

    @Id
    @Column(name = "message_id")
    private String messageId;

    @Column(name = "batch_id")
    private String batchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @Column(name = "sender")
    private String sender;

    @Column(name = "party_id")
    private String partyId;

    @Column(name = "party_contact")
    private String partyContact;

    @Lob
    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "history_external_references", joinColumns = @JoinColumn(name = "message_id"))
    private Map<String, String> externalReferences;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Generated  // Dirty "fix", since somehow leaving this out f*cks up the Jacoco coverage...
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
