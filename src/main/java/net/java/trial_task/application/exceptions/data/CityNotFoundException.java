package net.java.trial_task.application.exceptions.data;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CityNotFoundException extends RuntimeException {
  public CityNotFoundException(Long id) {
    super("City not found: id=" + id);
  }
}