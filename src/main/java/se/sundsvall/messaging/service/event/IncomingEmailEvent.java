package se.sundsvall.messaging.service.event;

public class IncomingEmailEvent extends Event {

    public IncomingEmailEvent(Object source, final String messageId) {
        super(source, messageId);
    }
}
