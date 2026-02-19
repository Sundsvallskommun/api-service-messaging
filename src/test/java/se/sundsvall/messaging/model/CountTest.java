package se.sundsvall.messaging.model;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountTest {

	private static final int SENT = RandomUtils.secure().randomInt();
	private static final int FAILED = RandomUtils.secure().randomInt();

	@Test
	void testConstructor() {
		final var bean = new Count(SENT, FAILED);

		assertBean(bean);
	}

	@Test
	void testBuilder() {
		final var bean = Count.builder()
			.withFailed(FAILED)
			.withSent(SENT)
			.build();

		assertBean(bean);
	}

	private void assertBean(final Count bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.sent()).isEqualTo(SENT);
		assertThat(bean.failed()).isEqualTo(FAILED);
		assertThat(bean.total()).isEqualTo(SENT + FAILED);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		final var bean = Count.builder().build();

		assertThat(bean.failed()).isZero();
		assertThat(bean.sent()).isZero();
		assertThat(bean.total()).isZero();
	}
}
