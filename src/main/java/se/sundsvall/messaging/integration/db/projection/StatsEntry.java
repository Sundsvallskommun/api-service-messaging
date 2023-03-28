package se.sundsvall.messaging.integration.db.projection;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

public record StatsEntry(
    MessageType messageType,
    MessageType originalMessageType,
    MessageStatus status) { }
