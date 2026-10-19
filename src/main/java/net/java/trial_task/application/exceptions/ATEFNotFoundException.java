package net.java.trial_task.application.exceptions;

public class ATEFNotFoundException extends RuntimeException {
  public ATEFNotFoundException(Long id) {
    super("ATEF not found: id=" + id);
  }
}
