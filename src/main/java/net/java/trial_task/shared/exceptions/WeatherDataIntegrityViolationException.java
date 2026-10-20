package net.java.trial_task.shared.exceptions;

public class WeatherDataIntegrityViolationException extends RuntimeException {
  public WeatherDataIntegrityViolationException(String message){
    super(message);
  }
}
