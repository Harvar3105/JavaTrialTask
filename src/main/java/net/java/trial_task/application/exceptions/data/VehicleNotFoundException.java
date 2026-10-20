package net.java.trial_task.application.exceptions.data;

import net.java.trial_task.application.exceptions.DeliveryFeeException;

public class VehicleNotFoundException extends DeliveryFeeException  {
  public VehicleNotFoundException(Long id) {
    super("Vehicle not found: id=" + id);
  }
  
  public VehicleNotFoundException(String vehicleType) {
    super("Vehicle type not found: " + vehicleType);
  }
}
