package se.sundsvall.messaging.service.event;

public class IncomingEmailEvent extends Event<String> {

    public IncomingEmailEvent(final Object source, final String deliveryId) {
        super(source, deliveryId);
    }
}
