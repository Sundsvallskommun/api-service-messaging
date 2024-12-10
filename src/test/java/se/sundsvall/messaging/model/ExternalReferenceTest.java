package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class ExternalReferenceTest {

	private static final String KEY = "key";
	private static final String VALUE = "value";

	@Test
	void testConstructor() {
		final var bean = new ExternalReference(KEY, VALUE);

		assertBean(bean);
	}

	@Test
	void testBuilder() {
		final var bean = ExternalReference.builder()
			.withKey(KEY)
			.withValue(VALUE)
			.build();

		assertBean(bean);
	}

	private void assertBean(final ExternalReference bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.key()).isEqualTo(KEY);
		assertThat(bean.value()).isEqualTo(VALUE);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ExternalReference.builder().build()).hasAllNullFieldsOrProperties();
	}
}
