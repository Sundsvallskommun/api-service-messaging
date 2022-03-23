package se.sundsvall.messaging.model.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
    private String id;
    private String messageId;
    private String batchId;
    @Enumerated(EnumType.STRING)
    private MessageType messageType;
    private String sender;
    private String partyId;
    private String partyContact;
    @Lob
    private String message;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
