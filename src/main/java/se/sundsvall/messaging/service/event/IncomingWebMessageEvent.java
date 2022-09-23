package se.sundsvall.messaging.service.event;

public class IncomingWebMessageEvent extends Event<String> {

    public IncomingWebMessageEvent(final Object source, final String deliveryId) {
        super(source, deliveryId);
    }
}
