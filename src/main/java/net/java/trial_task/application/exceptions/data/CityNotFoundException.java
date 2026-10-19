package net.java.trial_task.application.exceptions.data;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.java.trial_task.application.exceptions.DeliveryFeeException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CityNotFoundException extends DeliveryFeeException {
  public CityNotFoundException(Long id) {
    super("City not found: id=" + id);
  }
  public CityNotFoundException(String cityName) {
    super("City not found: " + cityName);
  }
}