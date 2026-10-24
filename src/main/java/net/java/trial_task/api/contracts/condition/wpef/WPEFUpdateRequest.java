package net.java.trial_task.api.contracts.condition.wpef;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import net.java.trial_task.application.domain.enums.Condition;

@Schema(description = """
  Payload for replacing a weather phenomenon extra fee (WPEF) rule. \
  The set of vehicle types is not modified by this operation. \
  Fee consistency: if 'forbidden' is true, 'fee' must be null or omitted; \
  if 'forbidden' is false, 'fee' is required.""")
public record WPEFUpdateRequest(
  @Schema(description = "Operator applied to the observed weather phenomenon; 'related' matches by keyword")
  @NotNull Condition condition,

  @Schema(description = "Phenomenon name or keyword the rule is matched against", example = "snow", maxLength = 255)
  @NotNull @Size (max = 255) String phenomenon,

  @Schema(description = "True if the rule forbids usage of the listed vehicle types; then 'fee' must be null", example = "false")
  @NotNull Boolean forbidden,

  @Schema(
    description = "Extra fee in EUR, zero or positive (up to 4 integer digits, 1 fractional digit); required when 'forbidden' is false",
    example = "1.0",
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

