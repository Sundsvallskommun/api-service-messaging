package se.sundsvall.messaging.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.sundsvall.messaging.api.model.response.UserMessage.Recipient;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

class FilterUtilsTest {
	private static final String PERSON_ID = "personId";

	@ParameterizedTest
	@MethodSource("isDigitalMailAndUnsuccessfulArgumentProvider")
	void isDigitalMailAndUnsuccessful(MessageType messageType, MessageStatus messageStatus, boolean result) {
		assertThat(FilterUtils.isDigitalMailAndUnsuccessful(Recipient.builder()
			.withMessageType(messageType.name())
			.withStatus(messageStatus.name())
			.build()))
			.isEqualTo(result);
	}

	private static Stream<Arguments> isDigitalMailAndUnsuccessfulArgumentProvider() {
		final List<Arguments> arguments = new ArrayList<>();

		// All combinations when messagetype is separated from DIGITAL_MAIL should not be true
		List.of(MessageType.values()).stream()
			.filter(messageType -> DIGITAL_MAIL != messageType)
			.forEach(messageType -> {
				List.of(MessageStatus.values()).stream()
					.forEach(messageStatus -> {
						arguments.add(Arguments.of(messageType, messageStatus, false));
					});
			});

		// Combination when messagetype is DIGITAL_MAIL and status is SENT should not be true
		arguments.add(Arguments.of(DIGITAL_MAIL, SENT, false));

		// Combination when messagetype is DIGITAL_MAIL and status is separated from SENT should be true
		List.of(MessageStatus.values()).stream()
			.filter(messageStatus -> SENT != messageStatus)
			.forEach(messageStatus -> {
				arguments.add(Arguments.of(DIGITAL_MAIL, messageStatus, true));
			});

		return arguments.stream();
	}

	@ParameterizedTest
	@MethodSource("isSnailMailSuccessfulParameterProvider")
	void isSnailMailSuccessful(String personId, MessageType messageType, MessageStatus messageStatus, boolean result) {
		assertThat(FilterUtils.isSnailMailSuccessful(PERSON_ID, List.of(Recipient.builder()
			.withPersonId(personId)
			.withMessageType(messageType.name())
			.withStatus(messageStatus.name())
			.build()))).isEqualTo(result);
	}

	private static Stream<Arguments> isSnailMailSuccessfulParameterProvider() {
		final List<Arguments> arguments = new ArrayList<>();

		// All combinations when personId is null should not be true
		List.of(MessageType.values()).stream()
			.forEach(messageType -> {
				List.of(MessageStatus.values()).stream()
					.forEach(messageStatus -> {
						arguments.add(Arguments.of(null, messageType, messageStatus, false));
					});
			});

		// All combinations when messagetype is separated from SNAIL_MAIL should not be true
		List.of(MessageType.values()).stream()
			.filter(messageType -> SNAIL_MAIL != messageType)
			.forEach(messageType -> {
				List.of(MessageStatus.values()).stream()
					.forEach(messageStatus -> {
						arguments.add(Arguments.of(PERSON_ID, messageType, messageStatus, false));
					});
			});

		// Combination when messagetype is SNAIL_MAIL and status is separated from SENT should be true
		List.of(MessageStatus.values()).stream()
			.filter(messageStatus -> SENT != messageStatus)
			.forEach(messageStatus -> {
				arguments.add(Arguments.of(PERSON_ID, SNAIL_MAIL, messageStatus, false));
			});

		// Combination when messagetype is SNAIL_MAIL and status is SENT should be true
		arguments.add(Arguments.of(PERSON_ID, SNAIL_MAIL, SENT, true));

		return arguments.stream();
	}
}
