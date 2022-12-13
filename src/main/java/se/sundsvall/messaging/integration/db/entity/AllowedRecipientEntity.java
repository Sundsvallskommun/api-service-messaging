package se.sundsvall.messaging.integration.db.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.IdClass;
import javax.persistence.Table;

import se.sundsvall.messaging.model.MessageType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "whitelisting")
@IdClass(AllowedRecipientEntity.Id.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
public class AllowedRecipientEntity {

    @javax.persistence.Id
    @Column(name = "recipient", nullable = false)
    private String recipient;

    @javax.persistence.Id
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(setterPrefix = "with")
    public static class Id implements Serializable {

        private String recipient;

        private MessageType messageType;

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            var other = (Id) o;
            return Objects.equals(recipient, other.recipient) && messageType == other.messageType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(recipient, messageType);
        }
    }
}
