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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "web_messages")
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class WebMessageEntity {

    @Id
    @Column(name = "message_id")
    private String messageId;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "party_id")
    private String partyId;

    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status;

    @Column(name = "sending_attempts_made")
    private int sendingAttempts;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "web_messages_external_references", joinColumns = @JoinColumn(name = "message_id"))
    private Map<String, String> externalReferences;

    @Generated  // Dirty "fix", since somehow leaving this out f*cks up the Jacoco coverage...
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
