package se.sundsvall.messaging.api.model.request;

/**
 * Envelope type for snail-mail.
 * To be used in {@link SnailMailRequest} or {@link LetterRequest}.
 */
public enum EnvelopeType {
	WINDOWED,
	PLAIN,
}
