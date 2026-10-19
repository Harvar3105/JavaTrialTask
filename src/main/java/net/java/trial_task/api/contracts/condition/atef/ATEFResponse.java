package net.java.trial_task.api.contracts.condition.atef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;

public record ATEFResponse(
  Long id,
  Condition condition,
  BigDecimal temperature,
  BigDecimal maxTemperature,
  boolean forbidden,
  BigDecimal fee,
  Set<Long> vehicleIds,
  Instant createdAt,
  Instant updatedAt
) {
  public static ATEFResponse from(ATEF atef) {
    return new ATEFResponse(
      atef.getId(),
      atef.getCondition(),
      atef.getTemperature(),
      atef.getMaxTemperature(),
      atef.checkIsForbidden(),
      atef.getFee(),
      atef.getVehicles().stream().map(Vehicle::getId).collect(Collectors.toSet()),
      atef.getCreatedAt(),
      atef.getUpdatedAt()
    );
  }
}
