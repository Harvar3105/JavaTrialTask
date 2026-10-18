package net.java.trial_task.api.contracts.city;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CityRequest(
  @NotBlank @Size(max = 255) String cityName
) {
}
