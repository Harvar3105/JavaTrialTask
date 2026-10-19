package net.java.trial_task.application.exceptions.data;

public class VehicleNotFoundException extends RuntimeException {
  public VehicleNotFoundException(Long id) {
    super("Vehicle not found: id=" + id);
  }
  public VehicleNotFoundException(String message) {
    super(message);
  }
}
