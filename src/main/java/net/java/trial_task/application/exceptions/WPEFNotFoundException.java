package net.java.trial_task.application.exceptions;

public class WPEFNotFoundException extends RuntimeException{
  public WPEFNotFoundException(Long id) {
    super("WPEF not found: id=" + id);
  }
}
