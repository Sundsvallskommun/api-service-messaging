package se.sundsvall.messaging.model.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

    private String batchId;
    @Id
    private String messageId;
    private String partyId;
    private String emailName;
    private String smsName;
    private String senderEmail;
    private String subject;
    private String message;
    @Enumerated(EnumType.STRING)
    private MessageType messageType;
    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus;
}
