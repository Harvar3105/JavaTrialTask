package net.java.trial_task.api.exceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import net.java.trial_task.api.controller.condition.RBFController;
import net.java.trial_task.application.exceptions.condition.RBFNotFoundException;

@RestControllerAdvice(assignableTypes = RBFController.class)
public class RBFExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler(RBFNotFoundException.class)
  public ProblemDetail handleNotFound(RBFNotFoundException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setTitle("RBF not found");
    return problem;
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Request violates a data integrity constraint");
    problem.setTitle("Data integrity violation");
    return problem;
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex,
    HttpHeaders headers,
    HttpStatusCode status,
    WebRequest request
  ) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult()
      .getFieldErrors()
      .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
    ProblemDetail problem = ex.getBody();
    problem.setProperty("errors", errors);
    return ResponseEntity.status(status).headers(headers).body(problem);
  }
}