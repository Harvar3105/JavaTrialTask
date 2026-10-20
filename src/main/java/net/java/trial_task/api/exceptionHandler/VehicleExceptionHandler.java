package net.java.trial_task.api.exceptionHandler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import net.java.trial_task.api.controller.data.VehicleController;
import net.java.trial_task.application.exceptions.data.VehicleAlreadyExistsException;
import net.java.trial_task.application.exceptions.data.VehicleNotFoundException;

@RestControllerAdvice(assignableTypes = VehicleController.class)
public class VehicleExceptionHandler {
  @ExceptionHandler(VehicleNotFoundException.class)
  public ProblemDetail notFound(VehicleNotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler({VehicleAlreadyExistsException.class, DataIntegrityViolationException.class})
  public ProblemDetail conflict(Exception e) {
    String detail = e instanceof VehicleAlreadyExistsException
      ? e.getMessage()
      : "Constraint violation";
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
  }
}
