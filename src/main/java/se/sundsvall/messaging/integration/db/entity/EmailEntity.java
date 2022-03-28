package se.sundsvall.messaging.integration.db.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import se.sundsvall.messaging.model.MessageStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "emails")
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class EmailEntity {

    @Id
    @Column(name = "message_id")
    private String messageId;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "party_id")
    private String partyId;

    @Column(name = "subject")
    private String subject;

    @Column(name = "message")
    private String message;

    @Lob
    @Column(name = "html_message")
    private String htmlMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(name = "sending_attempts_made")
    private int sendingAttempts;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "emails_external_references", joinColumns = @JoinColumn(name = "message_id"))
    private Map<String, String> externalReferences;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "message_id")
    private List<Attachment> attachments;

    @Generated  // Dirty "fix", since somehow leaving this out f*cks up the Jacoco coverage...
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    @Entity
    @Table(name = "emails_attachments")
    public static class Attachment {

        @Id
        @Column(name = "id")
        private String id;

        @Lob
        @Column(name = "content")
        private String content;

        @Column(name = "name")
        private String name;

        @Column(name = "content_type")
        private String contentType;
    }
}
