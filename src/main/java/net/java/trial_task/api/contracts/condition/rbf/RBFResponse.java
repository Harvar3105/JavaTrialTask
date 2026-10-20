package net.java.trial_task.api.contracts.condition.rbf;

import java.math.BigDecimal;
import java.time.Instant;

import net.java.trial_task.application.domain.entity.condition.RBF;

public record RBFResponse(
  Long id,
  Long cityId,
  Long vehicleTypeId,
  BigDecimal fee,
  Instant createdAt,
  Instant updatedAt
) {
  public static RBFResponse from(RBF e) {
    return new RBFResponse(
      e.getId(),
      e.getCityId(),
      e.getVehicleTypeId(),
      e.getFee(),
      e.getCreatedAt(),
      e.getUpdatedAt()
    );
  }
}
