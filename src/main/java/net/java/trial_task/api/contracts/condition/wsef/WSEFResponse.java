package net.java.trial_task.api.contracts.condition.wsef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;

public record WSEFResponse(
  Long id,
  Condition condition,
  BigDecimal windSpeed,
  BigDecimal maxWindSpeed,
  boolean forbidden,
  BigDecimal fee,
  Set<Long> vehicleIds,
  Instant createdAt,
  Instant updatedAt
) {
  public static WSEFResponse from(WSEF atef) {
    return new WSEFResponse(
      atef.getId(),
      atef.getCondition(),
      atef.getWindSpeed(),
      atef.getMaxWindSpeed(),
      atef.checkIsForbidden(),
      atef.getFee(),
      atef.getVehicles().stream().map(Vehicle::getId).collect(Collectors.toSet()),
      atef.getCreatedAt(),
      atef.getUpdatedAt()
    );
  }
}
