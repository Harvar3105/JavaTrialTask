package net.java.trial_task.application.exceptions.condition;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.java.trial_task.application.exceptions.DeliveryFeeException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RBFNotFoundException extends DeliveryFeeException  {
  public RBFNotFoundException(Long id){
    super("RBF not found. Id=" + id);
  }

  public RBFNotFoundException(String cityName, String vehicleType) {
    super("Regional base fee not found for city " + cityName + " and vehicle type " + vehicleType);
  }
}
