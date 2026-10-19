package net.java.trial_task.api.controller.condition;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import net.java.trial_task.api.contracts.condition.atef.ATEFCreateRequest;
import net.java.trial_task.api.contracts.condition.atef.ATEFResponse;
import net.java.trial_task.api.contracts.condition.atef.ATEFUpdateRequest;
import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.service.condition.ATEFService;

@RestController
@RequestMapping("/api/atef")
public class ATEFController {
  private final ATEFService atefService;

  public ATEFController(ATEFService atefService) {
    this.atefService = atefService;
  }

  @GetMapping
  public List<ATEFResponse> getAll() {
    return atefService.getAll().stream().map(ATEFResponse::from).toList();
  }

  @GetMapping("/{id}")
  public ATEFResponse getById(@PathVariable("id") Long id) {
    return ATEFResponse.from(atefService.getById(id));
  }

  @PostMapping
  public ResponseEntity<ATEFResponse> create(@Valid @RequestBody ATEFCreateRequest request) {
    ATEF created = atefService.create(
      request.condition(),
      request.temperature(),
      request.maxTemperature(),
      request.resolveFee(),
      request.vehicleIds()
    );

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}")
      .buildAndExpand(created.getId())
      .toUri();

    return ResponseEntity.created(location).body(ATEFResponse.from(created));
  }

  @PutMapping("/{id}")
  public ATEFResponse update(@PathVariable Long id, @Valid @RequestBody ATEFUpdateRequest request) {
    ATEF updated = atefService.update(
      id,
      request.condition(),
      request.temperature(),
      request.maxTemperature(),
      request.resolveFee()
    );
    return ATEFResponse.from(updated);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    atefService.delete(id);
  }
}
