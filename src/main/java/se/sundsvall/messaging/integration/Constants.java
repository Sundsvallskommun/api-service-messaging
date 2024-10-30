package se.sundsvall.messaging.integration;

import static java.time.Duration.ofSeconds;

import java.time.Duration;

public final class Constants {

	public static final Duration DEFAULT_CONNECT_TIMEOUT = ofSeconds(5);
	public static final Duration DEFAULT_READ_TIMEOUT = ofSeconds(15);

	private Constants() {}
}
