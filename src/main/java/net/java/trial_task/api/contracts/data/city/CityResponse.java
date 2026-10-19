package net.java.trial_task.api.contracts.data.city;

import java.time.Instant;

import net.java.trial_task.application.domain.entity.data.City;

public record CityResponse(Long id, String cityName, Instant createdAt, Instant updatedAt) {
  public static CityResponse from(City city) {
    return new CityResponse(city.getId(), city.getCityName(), city.getCreatedAt(), city.getUpdatedAt());
  }
}
