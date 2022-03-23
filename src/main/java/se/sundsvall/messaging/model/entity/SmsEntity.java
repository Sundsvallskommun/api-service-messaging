package se.sundsvall.messaging.model.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
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
@Table(name = "queued_smses")
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class SmsEntity {

    private String batchId;
    @Id
    private String messageId;
    private String sender;
    private String partyId;
    private String mobileNumber;
    private String message;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    private int sendingAttempts;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
