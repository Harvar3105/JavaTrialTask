package net.java.trial_task.weather.exceptions;


public class WeatherParseException extends RuntimeException {

  public WeatherParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
