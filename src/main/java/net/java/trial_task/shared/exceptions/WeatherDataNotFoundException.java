package net.java.trial_task.shared.exceptions;

import java.time.Instant;

import net.java.trial_task.application.exceptions.DeliveryFeeException;

public class WeatherDataNotFoundException extends DeliveryFeeException {
  public WeatherDataNotFoundException(String cityName, Instant at) {
    super("Weather data not found for city " + cityName + (at == null ? "" : " at " + at));
  }
}