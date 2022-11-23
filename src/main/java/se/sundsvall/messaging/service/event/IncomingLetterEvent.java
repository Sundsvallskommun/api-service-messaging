package se.sundsvall.messaging.service.event;

public class IncomingLetterEvent extends Event<String> {

    public IncomingLetterEvent(final Object source, final String deliveryId) {
        super(source, deliveryId);
    }
}
