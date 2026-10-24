package net.java.trial_task.api.contracts.data.vehicle;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import net.java.trial_task.application.domain.entity.data.Vehicle;

@Schema(description = "Vehicle type registered in the system")
public record VehicleResponse(
  @Schema(description = "Unique identifier, assigned by the database", example = "1")
  Long id,

  @Schema(description = "Creation timestamp, UTC (ISO-8601)", example = "2026-10-24T12:00:00Z")
  Instant createdAt,

  @Schema(description = "Last modification timestamp, UTC (ISO-8601)", example = "2026-10-24T12:00:00Z")
  Instant updatedAt,
  
  @Schema(description = "Vehicle type name", example = "Bike")
  String vehicleType
) {
  public static VehicleResponse from(Vehicle v) {
    return new VehicleResponse(v.getId(), v.getCreatedAt(), v.getUpdatedAt(), v.getVehicleType());
  }
}
