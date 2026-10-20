package net.java.trial_task.shared.exceptions;

public class WeatherDataRepositoryUnavailableException extends RuntimeException {
  public WeatherDataRepositoryUnavailableException(String message){
    super(message);
  }

  public WeatherDataRepositoryUnavailableException(String message, Throwable cause){
    super(message, cause);
  }
}
