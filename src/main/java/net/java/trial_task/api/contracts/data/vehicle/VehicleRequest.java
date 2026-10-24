package net.java.trial_task.api.contracts.data.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating or replacing a vehicle type")
public record VehicleRequest(
  @Schema(description = "Vehicle type name; must not be blank", example = "Bike", maxLength = 64)
  @NotBlank @Size(max = 64) String vehicleType
) {
  
}
