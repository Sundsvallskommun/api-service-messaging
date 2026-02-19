package se.sundsvall.messaging.integration;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

public final class Constants {

	public static final Duration DEFAULT_CONNECT_TIMEOUT = ofSeconds(5);
	public static final Duration DEFAULT_READ_TIMEOUT = ofSeconds(15);

	private Constants() {}
}
