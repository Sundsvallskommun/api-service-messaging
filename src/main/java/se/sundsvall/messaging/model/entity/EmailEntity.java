package se.sundsvall.messaging.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import se.sundsvall.messaging.api.MessageStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "queued_emails")
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class EmailEntity {

    private String batchId;
    @Id
    private String messageId;
    private String emailAddress;
    private String partyId;
    private String subject;
    private String message;
    private String htmlMessage;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    private String senderName;
    private String senderEmail;
    private int sendingAttempts;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "email_id")
    private List<Attachment> attachments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    @Entity
    @Table(name = "queued_emails_attachments")
    public static class Attachment {

        @Id
        private String id;
        private String content;
        private String name;
        private String contentType;
    }
}
