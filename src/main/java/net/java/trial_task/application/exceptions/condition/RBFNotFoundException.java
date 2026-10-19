package net.java.trial_task.application.exceptions.condition;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RBFNotFoundException extends RuntimeException {
  public RBFNotFoundException(Long id){
    super("RBF not found. Id=" + id);
  }
}
