package net.java.trial_task.api.contracts.condition.wpef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;

public record WPEFResponse(
  Long id,
  Condition condition,
  String phenomenon,
  boolean forbidden,
  BigDecimal fee,
  Set<Long> vehicleIds,
  Instant createdAt,
  Instant updatedAt
) {
  public static WPEFResponse from(WPEF wpef) {
    return new WPEFResponse(
      wpef.getId(),
      wpef.getCondition(),
      wpef.getPhenomenon(),
      wpef.checkIsForbidden(),
      wpef.getFee(),
      wpef.getVehicles().stream().map(Vehicle::getId).collect(Collectors.toSet()),
      wpef.getCreatedAt(),
      wpef.getUpdatedAt()
    );
  }
}
