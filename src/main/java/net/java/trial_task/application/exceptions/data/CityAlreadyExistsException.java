package net.java.trial_task.application.exceptions.data;

public class CityAlreadyExistsException extends RuntimeException {
  public CityAlreadyExistsException(String cityName){
    super("City already exists with name: " + cityName);
  }
}
