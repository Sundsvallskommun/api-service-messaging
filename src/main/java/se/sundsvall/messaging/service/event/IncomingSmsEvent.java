package se.sundsvall.messaging.service.event;

public class IncomingSmsEvent extends Event {

    public IncomingSmsEvent(Object source, final String messageId) {
        super(source, messageId);
    }
}
