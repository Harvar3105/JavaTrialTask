package net.java.trial_task.api.rbf;

import java.net.URI;
import java.util.List;

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
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import net.java.trial_task.api.contracts.rbf.RBFCreateRequest;
import net.java.trial_task.api.contracts.rbf.RBFResponse;
import net.java.trial_task.api.contracts.rbf.RBFUpdateRequest;
import net.java.trial_task.application.domain.entity.condition.RBF;
import net.java.trial_task.application.service.RBFService;

@RestController
@RequestMapping("/api/rbf")
public class RBFController {
  private final RBFService service;

  public RBFController(RBFService service) {
    this.service = service;
  }

  @GetMapping
  public List<RBFResponse> findAll() {
    return service.findAll().stream().map(RBFResponse::from).toList();
  }

  @GetMapping("/{id}")
  public RBFResponse findById(@PathVariable("id") Long id) {
    return RBFResponse.from(service.findById(id));
  }

  @PostMapping
  public ResponseEntity<RBFResponse> create(@Valid @RequestBody RBFCreateRequest request) {
    RBF created = service.create(request.cityId(), request.vehicleTypeId(), request.fee());
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(created.getId()).toUri();
    return ResponseEntity.created(location).body(RBFResponse.from(created));
  }

  @PutMapping("/{id}")
  public RBFResponse updateFee(
    @PathVariable("id") Long id,
    @Valid @RequestBody RBFUpdateRequest request
  ) {
    return RBFResponse.from(service.updateFee(id, request.fee()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}