package net.java.trial_task.weather.exceptions;

public class WeatherDataUnavailableException extends RuntimeException {
  public WeatherDataUnavailableException(String message) {
    super(message);
  }

  public WeatherDataUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
