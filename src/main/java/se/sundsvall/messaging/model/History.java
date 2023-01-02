package se.sundsvall.messaging.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record History(
    String batchId,
    String messageId,
    String deliveryId,
    MessageType messageType,
    MessageStatus status,
    String content,
    LocalDateTime createdAt) {  }
