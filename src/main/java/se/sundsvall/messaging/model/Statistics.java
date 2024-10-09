package se.sundsvall.messaging.model;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with")
public record Statistics(

	@JsonProperty("EMAIL") Count email,

	@JsonProperty("SMS") Count sms,

	@JsonProperty("WEB_MESSAGE") Count webMessage,

	@JsonProperty("DIGITAL_MAIL") Count digitalMail,

	@JsonProperty("SNAIL_MAIL") Count snailMail,

	@JsonProperty("MESSAGE") Message message,

	@JsonProperty("LETTER") Letter letter) {

	public int total() {
		return ofNullable(email).map(Count::total).orElse(0) +
			ofNullable(sms).map(Count::total).orElse(0) +
			ofNullable(webMessage).map(Count::total).orElse(0) +
			ofNullable(digitalMail).map(Count::total).orElse(0) +
			ofNullable(snailMail).map(Count::total).orElse(0) +
			ofNullable(message).map(Message::total).orElse(0) +
			ofNullable(letter).map(Letter::total).orElse(0);
	}

	@Builder(setterPrefix = "with")
	@Schema(name = "MessageStatistics")
	@JsonIgnoreProperties("total")
	public record Message(
		@JsonProperty("EMAIL") Count email,

		@JsonProperty("SMS") Count sms,

		@JsonProperty("UNDELIVERABLE") Integer undeliverable) {

		public int total() {
			return ofNullable(email).map(Count::total).orElse(0) +
				ofNullable(sms).map(Count::total).orElse(0) +
				ofNullable(undeliverable).orElse(0);
		}
	}

	@Builder(setterPrefix = "with")
	@Schema(name = "LetterStatistics")
	@JsonIgnoreProperties("total")
	public record Letter(

		@JsonProperty("SNAIL_MAIL") Count snailMail,

		@JsonProperty("DIGITAL_MAIL") Count digitalMail) {

		public int total() {
			return ofNullable(digitalMail).map(Count::total).orElse(0) +
				ofNullable(snailMail).map(Count::total).orElse(0);
		}
	}
}
