package net.java.trial_task.api.contracts.condition.rbf;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;

public record RBFUpdateRequest (
  @Digits(integer = 4, fraction = 1) BigDecimal fee
) {

}
