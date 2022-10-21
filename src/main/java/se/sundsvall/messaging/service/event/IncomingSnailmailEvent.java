package se.sundsvall.messaging.service.event;

public class IncomingSnailmailEvent extends Event<String> {

    public IncomingSnailmailEvent(final Object source, final String deliveryId) {
        super(source, deliveryId);
    }
}
