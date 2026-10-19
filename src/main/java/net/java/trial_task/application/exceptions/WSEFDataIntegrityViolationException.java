package net.java.trial_task.application.exceptions;

public class WSEFDataIntegrityViolationException extends RuntimeException {
  public WSEFDataIntegrityViolationException(String message){
    super(message);
  }
}
