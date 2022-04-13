package se.sundsvall.messaging.service.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public abstract class Event extends ApplicationEvent {

    private final String messageId;

    protected Event(final Object source, final String messageId) {
        super(source);

        this.messageId = messageId;
    }
}
