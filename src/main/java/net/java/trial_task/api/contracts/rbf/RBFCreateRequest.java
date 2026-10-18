package net.java.trial_task.api.contracts.rbf;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record RBFCreateRequest(
  @NotNull Long cityId,
  @NotNull Long vehicleTypeId,
  @Digits(integer = 4, fraction = 1) BigDecimal fee
) {

  }
