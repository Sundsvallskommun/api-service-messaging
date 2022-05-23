package se.sundsvall.messaging.api.model;

import java.util.List;

import javax.validation.Valid;

import se.sundsvall.messaging.model.Header;
import se.sundsvall.messaging.model.Party;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Request {

    @Valid
    @Schema(description = "Party")
    private Party party;

    @Schema(description = "Headers")
    private List<@Valid Header> headers;
}
