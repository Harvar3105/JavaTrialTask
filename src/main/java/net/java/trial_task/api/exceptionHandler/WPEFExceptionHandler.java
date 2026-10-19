package net.java.trial_task.api.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import net.java.trial_task.api.wpef.WPEFController;
import net.java.trial_task.application.exceptions.WPEFDataIntegrityViolationException;
import net.java.trial_task.application.exceptions.WPEFNotFoundException;

@RestControllerAdvice(assignableTypes = WPEFController.class)
public class WPEFExceptionHandler {
  @ExceptionHandler(WPEFNotFoundException.class)
  public ProblemDetail handleNotFound(WPEFNotFoundException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(WPEFDataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolation(WPEFDataIntegrityViolationException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
  }
}
