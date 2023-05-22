package se.sundsvall.messaging.test.assertj;

public class Assertions extends org.assertj.core.api.Assertions {

    public static StringAssert assertThat(final String actual) {
        return new StringAssert(actual);
    }
}
