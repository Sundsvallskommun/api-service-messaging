package se.sundsvall.messaging.service.event;

public class IncomingWebMessageEvent extends Event {

    public IncomingWebMessageEvent(Object source, final String messageId) {
        super(source, messageId);
    }
}
