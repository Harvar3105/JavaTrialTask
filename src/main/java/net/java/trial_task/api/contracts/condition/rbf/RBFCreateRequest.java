package net.java.trial_task.api.contracts.condition.rbf;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for creating a regional base fee (RBF) for a city and vehicle type pair")
public record RBFCreateRequest(
  @Schema(description = "Identifier of an existing city; immutable after creation", example = "1")
  @NotNull Long cityId,

  @Schema(description = "Identifier of an existing vehicle type; immutable after creation", example = "2")
  @NotNull Long vehicleTypeId,

  @Schema(
    description = """
    Base fee in EUR: up to 4 integer digits and 1 fractional digit. \
    Null or omitted means usage of the vehicle type in the city is forbidden.""",
    example = "3.5",
    nullable = true
  )
  @Digits(integer = 4, fraction = 1) BigDecimal fee
) {

}
