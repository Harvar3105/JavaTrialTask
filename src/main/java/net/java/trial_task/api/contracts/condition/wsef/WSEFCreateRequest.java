package net.java.trial_task.api.contracts.condition.wsef;

import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import net.java.trial_task.application.domain.enums.Condition;

@Schema(description = """
  Payload for creating a wind speed extra fee (WSEF) rule. \
  Fee consistency: if 'forbidden' is true, 'fee' must be null or omitted; \
  if 'forbidden' is false, 'fee' is required.""")
public record WSEFCreateRequest(
  @Schema(description = "Comparison operator applied to the observed wind speed")
  @NotNull Condition condition,

  @Schema(
    description = "Threshold wind speed in m/s (up to 4 integer digits, 1 fractional digit); lower bound for range operators",
    example = "10"
  )
  @NotNull @Digits(integer = 4, fraction = 1) BigDecimal windSpeed,

  @Schema(
    description = "Upper bound wind speed in m/s (up to 4 integer digits, 1 fractional digit); used by range operators",
    example = "20",
    nullable = true
  )
  @Digits(integer = 4, fraction = 1) BigDecimal maxWindSpeed,

  @Schema(description = "True if the rule forbids usage of the listed vehicle types; then 'fee' must be null", example = "false")
  @NotNull Boolean forbidden,

  @Schema(
    description = "Extra fee in EUR, zero or positive (up to 4 integer digits, 1 fractional digit); required when 'forbidden' is false",
    example = "0.5",
    nullable = true
  )
  @PositiveOrZero @Digits(integer = 4, fraction = 1) BigDecimal fee,

  @Schema(description = "Identifiers of existing vehicle types to which the rule applies")
  Set<Long> vehicleIds
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
