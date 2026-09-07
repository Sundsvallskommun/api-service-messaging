package se.sundsvall.messaging.integration.rabbitmq;

/**
 * Messaging's own message and batch ids for one logical send request, held together so an attempt can hand them to the
 * next. Not to be confused with the ids the queue message carries, which are postportal's.
 */
public record MessageIds(String messageId, String batchId) {
}
