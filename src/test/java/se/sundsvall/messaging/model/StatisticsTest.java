package se.sundsvall.messaging.model;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.Statistics.Letter;
import se.sundsvall.messaging.model.Statistics.Message;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsTest {

	private static final int UNDERLIVERABLE = RandomUtils.secure().randomInt();
	private static final Count EMAIL = Count.builder().withFailed(RandomUtils.secure().randomInt()).withSent(RandomUtils.secure().randomInt()).build();
	private static final Count SMS = Count.builder().withFailed(RandomUtils.secure().randomInt()).withSent(RandomUtils.secure().randomInt()).build();
	private static final Count WEB_MESSAGE = Count.builder().withFailed(RandomUtils.secure().randomInt()).withSent(RandomUtils.secure().randomInt()).build();
	private static final Count DIGITAL_MAIL = Count.builder().withFailed(RandomUtils.secure().randomInt()).withSent(RandomUtils.secure().randomInt()).build();
	private static final Count SNAIL_MAIL = Count.builder().withFailed(RandomUtils.secure().randomInt()).withSent(RandomUtils.secure().randomInt()).build();
	private static final Message MESSAGE = Message.builder().withEmail(EMAIL).withSms(SMS).withUndeliverable(UNDERLIVERABLE).build();
	private static final Letter LETTER = Letter.builder().withDigitalMail(DIGITAL_MAIL).withSnailMail(SNAIL_MAIL).build();

	// Statistics
	@Test
	void testStatisticsConstructor() {
		final var bean = new Statistics(EMAIL, SMS, WEB_MESSAGE, DIGITAL_MAIL, SNAIL_MAIL, MESSAGE, LETTER);

		assertStatistics(bean);
	}

	@Test
	void testStatisticsBuilder() {
		final var bean = Statistics.builder()
			.withDigitalMail(DIGITAL_MAIL)
			.withEmail(EMAIL)
			.withLetter(LETTER)
			.withMessage(MESSAGE)
			.withSms(SMS)
			.withSnailMail(SNAIL_MAIL)
			.withWebMessage(WEB_MESSAGE)
			.build();

		assertStatistics(bean);
	}

	private void assertStatistics(final Statistics bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.digitalMail()).isEqualTo(DIGITAL_MAIL);
		assertThat(bean.email()).isEqualTo(EMAIL);
		assertThat(bean.letter()).isEqualTo(LETTER);
		assertThat(bean.message()).isEqualTo(MESSAGE);
		assertThat(bean.sms()).isEqualTo(SMS);
		assertThat(bean.snailMail()).isEqualTo(SNAIL_MAIL);
		assertThat(bean.webMessage()).isEqualTo(WEB_MESSAGE);
		assertThat(bean.total()).isEqualTo(DIGITAL_MAIL.total() + EMAIL.total() + LETTER.total() + MESSAGE.total() + SMS.total() + SNAIL_MAIL.total() + WEB_MESSAGE.total());
	}

	// Statistics.Letter
	@Test
	void testStatisticsLetterConstructor() {
		final var bean = new Statistics.Letter(SNAIL_MAIL, DIGITAL_MAIL);

		assertStatisticsLetter(bean);
	}

	@Test
	void testStatisticsLetterBuilder() {
		final var bean = Statistics.Letter.builder()
			.withDigitalMail(DIGITAL_MAIL)
			.withSnailMail(SNAIL_MAIL)
			.build();

		assertStatisticsLetter(bean);
	}

	private void assertStatisticsLetter(final Letter bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.digitalMail()).isEqualTo(DIGITAL_MAIL);
		assertThat(bean.snailMail()).isEqualTo(SNAIL_MAIL);
		assertThat(bean.total()).isEqualTo(DIGITAL_MAIL.total() + SNAIL_MAIL.total());
	}

	// Statistics.Message
	@Test
	void testStatisticsMessageConstructor() {
		final var bean = new Statistics.Message(EMAIL, SMS, UNDERLIVERABLE);

		assertStatisticsMessage(bean);
	}

	@Test
	void testStatisticsMessageBUilder() {
		final var bean = Statistics.Message.builder()
			.withEmail(EMAIL)
			.withSms(SMS)
			.withUndeliverable(UNDERLIVERABLE)
			.build();

		assertStatisticsMessage(bean);
	}

	private void assertStatisticsMessage(final Message bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.email()).isEqualTo(EMAIL);
		assertThat(bean.sms()).isEqualTo(SMS);
		assertThat(bean.undeliverable()).isEqualTo(UNDERLIVERABLE);
		assertThat(bean.total()).isEqualTo(EMAIL.total() + SMS.total() + UNDERLIVERABLE);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(Statistics.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(Statistics.builder().build().total()).isZero();
		assertThat(Statistics.Letter.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(Statistics.Letter.builder().build().total()).isZero();
		assertThat(Statistics.Message.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(Statistics.Message.builder().build().total()).isZero();
	}
}
