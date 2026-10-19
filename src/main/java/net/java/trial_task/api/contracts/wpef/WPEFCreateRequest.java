package net.java.trial_task.api.contracts.wpef;

import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import net.java.trial_task.application.domain.enums.Condition;

public record WPEFCreateRequest(
  @NotNull Condition condition,
  @NotNull @Size(max = 255) String phenomenon,
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
