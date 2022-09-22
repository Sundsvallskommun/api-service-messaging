package se.sundsvall.messaging.service.event;

public class IncomingMessageEvent extends Event<Long> {

    public IncomingMessageEvent(final Object source, final Long id) {
        super(source, id);
    }
}
