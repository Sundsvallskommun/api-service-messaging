package se.sundsvall.messaging.model;

public enum MessageStatus {
    PENDING,
    AWAITING_FEEDBACK,
    SENT,
    NOT_SENT,
    FAILED,
    NO_FEEDBACK_SETTINGS_FOUND,
    NO_FEEDBACK_WANTED
}
