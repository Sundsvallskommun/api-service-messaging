package se.sundsvall.messaging.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
@Schema(name = "Address")
public record Address(

	@Schema(description = "The first name of the recipient", example = "John") String firstName,

	@Schema(description = "The last name of the recipient", example = "Doe") String lastName,

	@Schema(description = "The address", example = "Main Street 1") String address,

	@Schema(description = "The apartment number", example = "1101") String apartmentNumber,

	@Schema(description = "The care of", example = "c/o John Doe") String careOf,

	@Schema(description = "The zip code", example = "12345") String zipCode,

	@Schema(description = "The city", example = "Main Street") String city,

	@Schema(description = "The country", example = "Sweden") String country) {
}
