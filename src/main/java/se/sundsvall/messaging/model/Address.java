package se.sundsvall.messaging.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
@Schema(name = "Address")
public record Address(

	@Schema(description = "The first name of the recipient", examples = "John") String firstName,

	@Schema(description = "The last name of the recipient", examples = "Doe") String lastName,

	@Schema(description = "The address", examples = "Main Street 1") String address,

	@Schema(description = "The apartment number", examples = "1101") String apartmentNumber,

	@Schema(description = "The care of", examples = "c/o John Doe") String careOf,

	@Schema(description = "The zip code", examples = "12345") String zipCode,

	@Schema(description = "The city", examples = "Main Street") String city,

	@Schema(description = "The country", examples = "Sweden") String country) {
}
