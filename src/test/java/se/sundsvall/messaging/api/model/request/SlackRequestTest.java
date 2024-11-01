package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SlackRequestTest {

	private static final String TOKEN = "token";
	private static final String CHANNEL = "channel";
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final String MESSAGE = "message";
	private static final String MUNICIPALITY_ID = "municipalityId";

	// SlackRequest
	@Test
	void testSlackRequestConstructor() {
		final var bean = new SlackRequest(TOKEN, CHANNEL, ORIGIN, ISSUER, MESSAGE, MUNICIPALITY_ID);

		assertSlackRequest(bean);
	}

	@Test
	void testSlackRequestBuilder() {
		final var bean = SlackRequest.builder()
			.withChannel(CHANNEL)
			.withIssuer(ISSUER)
			.withMessage(MESSAGE)
			.withOrigin(ORIGIN)
			.withToken(TOKEN)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertSlackRequest(bean);
	}

	private void assertSlackRequest(final SlackRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.channel()).isEqualTo(CHANNEL);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.message()).isEqualTo(MESSAGE);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.token()).isEqualTo(TOKEN);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SlackRequest.builder().build()).hasAllNullFieldsOrProperties();
	}
}
