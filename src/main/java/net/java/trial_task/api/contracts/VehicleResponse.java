package net.java.trial_task.api.contracts;

import java.time.Instant;

import net.java.trial_task.shared.domain.entity.data.Vehicle;

public record VehicleResponse(
  Long id,
  Instant createdAt,
  Instant updatedAt,
  String vehicleType
) {
  public static VehicleResponse from(Vehicle v) {
    return new VehicleResponse(v.getId(), v.getCreatedAt(), v.getUpdatedAt(), v.getVehicleType());
  }
}
