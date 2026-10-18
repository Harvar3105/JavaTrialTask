package net.java.trial_task.application.exceptions;

public class CityAlreadyExistsException extends RuntimeException {
  public CityAlreadyExistsException(String cityName){
    super("City already exists with name: " + cityName);
  }
}
