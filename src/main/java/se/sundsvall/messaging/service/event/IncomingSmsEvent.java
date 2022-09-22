package se.sundsvall.messaging.service.event;

public class IncomingSmsEvent extends Event<String> {

    public IncomingSmsEvent(final Object source, final String deliveryId) {
        super(source, deliveryId);
    }
}
