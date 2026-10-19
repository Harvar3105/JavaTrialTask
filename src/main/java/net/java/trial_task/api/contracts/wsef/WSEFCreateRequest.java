package net.java.trial_task.api.contracts.wsef;

import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import net.java.trial_task.application.domain.enums.Condition;

public record WSEFCreateRequest(
  @NotNull Condition condition,
  @NotNull @Digits(integer = 4, fraction = 1) BigDecimal windSpeed,
  @Digits(integer = 4, fraction = 1) BigDecimal maxWindSpeed,
  @NotNull Boolean forbidden,
  @PositiveOrZero @Digits(integer = 4, fraction = 1) BigDecimal fee,
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
