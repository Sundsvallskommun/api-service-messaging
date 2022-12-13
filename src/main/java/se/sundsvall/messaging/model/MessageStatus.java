package se.sundsvall.messaging.model;

public enum MessageStatus {
    AWAITING_FEEDBACK,
    PENDING,
    SENT,
    FAILED,
    NOT_WHITELISTED,
    NO_FEEDBACK_SETTINGS_FOUND,
    NO_FEEDBACK_WANTED
}
