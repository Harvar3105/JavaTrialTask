package net.java.trial_task.api.contracts.condition.atef;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import net.java.trial_task.application.domain.enums.Condition;

@Schema(description = """
  Payload for replacing an air temperature extra fee (ATEF) rule. \
  The set of vehicle types is not modified by this operation. \
  Fee consistency: if 'forbidden' is true, 'fee' must be null or omitted; \
  if 'forbidden' is false, 'fee' is required.""")
public record ATEFUpdateRequest(
  @Schema(description = "Comparison operator applied to the observed air temperature; 'related' is not supported for ATEF")
  @NotNull Condition condition,

  @Schema(
    description = "Threshold air temperature in °C (up to 4 integer digits, 1 fractional digit); lower bound for range operators",
    example = "-10"
  )
  @NotNull @Digits(integer = 4, fraction = 1) BigDecimal temperature,

  @Schema(
    description = "Upper bound air temperature in °C (up to 4 integer digits, 1 fractional digit); used by range operators",
    example = "0",
    nullable = true
  )
  @Digits(integer = 4, fraction = 1) BigDecimal maxTemperature,

  @Schema(description = "True if the rule forbids usage of the listed vehicle types; then 'fee' must be null", example = "false")
  @NotNull Boolean forbidden,

  @Schema(
    description = "Extra fee in EUR, zero or positive (up to 4 integer digits, 1 fractional digit); required when 'forbidden' is false",
    example = "0.5",
    nullable = true
  )
  @PositiveOrZero @Digits(integer = 4, fraction = 1) BigDecimal fee
) {
  @JsonIgnore
  @AssertTrue(message = "fee must be null if forbidden is true and must be provided if forbidden is false")
  public boolean isFeeConsistent() {
    if (forbidden == null) return true;
    return forbidden ? fee == null : fee != null;
  }

  public BigDecimal resolveFee() {
    return Boolean.TRUE.equals(forbidden) ? null : fee;
  }
}
