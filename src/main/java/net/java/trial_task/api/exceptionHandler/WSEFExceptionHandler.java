package net.java.trial_task.api.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import net.java.trial_task.api.wsef.WSEFController;
import net.java.trial_task.application.exceptions.WSEFDataIntegrityViolationException;
import net.java.trial_task.application.exceptions.WSEFNotFoundException;

@RestControllerAdvice(assignableTypes = WSEFController.class)
public class WSEFExceptionHandler {
  @ExceptionHandler(WSEFNotFoundException.class)
  public ProblemDetail handleNotFound(WSEFNotFoundException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(WSEFDataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolation(WSEFDataIntegrityViolationException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
  }
}
