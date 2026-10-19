package net.java.trial_task.api.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import net.java.trial_task.api.controller.condition.ATEFController;
import net.java.trial_task.application.exceptions.condition.ATEFDataIntegrityViolationException;
import net.java.trial_task.application.exceptions.condition.ATEFNotFoundException;

@RestControllerAdvice(assignableTypes = ATEFController.class)
public class ATEFControllerAdvice {
  @ExceptionHandler(ATEFNotFoundException.class)
  public ProblemDetail handleNotFound(ATEFNotFoundException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ATEFDataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolation(ATEFDataIntegrityViolationException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
  }
}
