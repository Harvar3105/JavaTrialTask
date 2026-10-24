package net.java.trial_task.api.contracts.data.city;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import net.java.trial_task.application.domain.entity.data.City;

@Schema(description = "City for which delivery fees are calculated")
public record CityResponse(
  @Schema(description = "Unique identifier, assigned by the database", example = "1")
  Long id,

  @Schema(description = "City name", example = "Tartu")
  String cityName,

  @Schema(description = "Creation timestamp, UTC (ISO-8601)", example = "2026-10-24T12:00:00Z")
  Instant createdAt,
  
  @Schema(description = "Last modification timestamp, UTC (ISO-8601)", example = "2026-10-24T12:00:00Z")
  Instant updatedAt
) {
  public static CityResponse from(City city) {
    return new CityResponse(city.getId(), city.getCityName(), city.getCreatedAt(), city.getUpdatedAt());
  }
}
