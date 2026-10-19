package net.java.trial_task.application.exceptions.data;

import net.java.trial_task.application.exceptions.DeliveryFeeException;

public class VehicleUsageForbiddenException extends DeliveryFeeException {
  public VehicleUsageForbiddenException() {
    super("Usage of selected vehicle type is forbidden");
  }
}