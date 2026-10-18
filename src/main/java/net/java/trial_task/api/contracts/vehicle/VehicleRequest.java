package net.java.trial_task.api.contracts.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
  @NotBlank @Size(max = 64) String vehicleType
) {
  
}
