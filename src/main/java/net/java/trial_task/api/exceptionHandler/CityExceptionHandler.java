package net.java.trial_task.api.exceptionHandler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import net.java.trial_task.api.city.CityController;
import net.java.trial_task.application.exceptions.CityAlreadyExistsException;
import net.java.trial_task.application.exceptions.CityNotFoundException;

@RestControllerAdvice(assignableTypes = CityController.class)
public class CityExceptionHandler {
  @ExceptionHandler(CityNotFoundException.class)
  public ProblemDetail notFound(CityNotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler({CityAlreadyExistsException.class, DataIntegrityViolationException.class})
  public ProblemDetail conflict(Exception e) {
    String detail = e instanceof CityAlreadyExistsException
      ? e.getMessage()
      : "Constraint violation";
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
  }
}