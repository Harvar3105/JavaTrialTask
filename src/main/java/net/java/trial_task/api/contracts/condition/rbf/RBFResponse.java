package net.java.trial_task.api.contracts.condition.rbf;

import java.math.BigDecimal;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import net.java.trial_task.application.domain.entity.condition.RBF;

@Schema(description = "Regional base fee (RBF) for a city and vehicle type pair")
public record RBFResponse(
  @Schema(description = "Unique identifier, assigned by the database", example = "1")
  Long id,

  @Schema(description = "Identifier of the city the fee applies to; immutable after creation", example = "1")
  Long cityId,

  @Schema(description = "Identifier of the vehicle type the fee applies to; immutable after creation", example = "2")
  Long vehicleTypeId,

  @Schema(
    description = "Base fee in EUR, scale 1. Null means usage of the vehicle type in the city is forbidden",
    example = "3.5",
    nullable = true
  )
  BigDecimal fee,

  @Schema(description = "Creation timestamp, UTC (ISO-8601)", example = "2026-10-24T12:00:00Z")
  Instant createdAt,

  @Schema(description = "Last modification timestamp, UTC (ISO-8601)", example = "2026-10-24T12:00:00Z")
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
