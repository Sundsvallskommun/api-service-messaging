package se.sundsvall.messaging.service.event;

public class IncomingMessageEvent extends Event {

    public IncomingMessageEvent(Object source, final String messageId) {
        super(source, messageId);
    }
}
