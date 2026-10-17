package net.java.trial_task.application.exceptions;

public class VehicleAlreadyExistsException extends RuntimeException {
  public VehicleAlreadyExistsException(String vehicleType) {
    super("Vehicle already exists: vehicleType=" + vehicleType);
  }
}
