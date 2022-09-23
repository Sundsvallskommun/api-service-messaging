package se.sundsvall.messaging.service.event;

public class IncomingDigitalMailEvent extends Event<String> {

    public IncomingDigitalMailEvent(final Object source, final String messageId) {
        super(source, messageId);
    }
}
