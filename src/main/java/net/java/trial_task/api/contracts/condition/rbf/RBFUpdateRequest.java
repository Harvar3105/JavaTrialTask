package net.java.trial_task.api.contracts.condition.rbf;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;

@Schema(description = "Payload for updating a regional base fee; only the fee can be changed")
public record RBFUpdateRequest (
  @Schema(
    description = """
    New base fee in EUR: up to 4 integer digits and 1 fractional digit. \
    Null or omitted means usage of the vehicle type in the city is forbidden.""",
    example = "3.5",
    nullable = true
  )
  @Digits(integer = 4, fraction = 1) BigDecimal fee
) {

}
