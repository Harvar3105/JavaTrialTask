package net.java.trial_task.api.contracts.data.city;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating or replacing a city")
public record CityRequest(
  @Schema(description = "City name; must not be blank", example = "Tartu", maxLength = 255)
  @NotBlank @Size(max = 255) String cityName
) {
}
