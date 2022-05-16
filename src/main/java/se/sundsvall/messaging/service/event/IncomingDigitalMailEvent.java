package se.sundsvall.messaging.service.event;

public class IncomingDigitalMailEvent extends Event {

    public IncomingDigitalMailEvent(Object source, final String messageId) {
        super(source, messageId);
    }
}
