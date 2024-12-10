package se.sundsvall.messaging.test.assertj;

import java.util.UUID;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.AssertionInfo;
import org.assertj.core.error.BasicErrorMessageFactory;
import org.assertj.core.internal.Failures;
import org.assertj.core.internal.Objects;
import org.assertj.core.internal.StandardComparisonStrategy;

public class StringAssert extends AbstractStringAssert<StringAssert> {

	private final Strings strings = Strings.instance();

	public StringAssert(final String actual) {
		super(actual, StringAssert.class);
	}

	/**
	 * Verifies that the actual value is a valid UUID (and, implicitly non-null).
	 *
	 * @return this assertion object
	 */
	public StringAssert isValidUuid() {
		strings.assertValidUuid(info, actual);
		return this;
	}

	static class Strings extends org.assertj.core.internal.Strings {

		private static final Strings INSTANCE = new Strings();

		private final Objects objects = Objects.instance();
		private final Failures failures = Failures.instance();

		Strings() {
			super(StandardComparisonStrategy.instance());
		}

		public static Strings instance() {
			return INSTANCE;
		}

		void assertValidUuid(final AssertionInfo info, final String actual) {
			objects.assertNotNull(info, actual);

			try {
				UUID.fromString(actual);
			} catch (Exception e) {
				throw failures.failure(info, new BasicErrorMessageFactory("%nExpecting UUID but was: %s", actual));
			}
		}
	}
}
