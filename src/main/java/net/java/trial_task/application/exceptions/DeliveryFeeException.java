package net.java.trial_task.application.exceptions;

public abstract class DeliveryFeeException extends RuntimeException {
  protected DeliveryFeeException(String message) { super(message); }
}
