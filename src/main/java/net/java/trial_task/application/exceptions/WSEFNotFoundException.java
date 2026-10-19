package net.java.trial_task.application.exceptions;

public class WSEFNotFoundException extends RuntimeException {
  public WSEFNotFoundException(Long id) {
    super("WSEF not found: id=" + id);
  }
}

