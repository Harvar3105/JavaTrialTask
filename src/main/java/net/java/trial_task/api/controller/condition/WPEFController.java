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
import net.java.trial_task.api.contracts.condition.wpef.WPEFCreateRequest;
import net.java.trial_task.api.contracts.condition.wpef.WPEFResponse;
import net.java.trial_task.api.contracts.condition.wpef.WPEFUpdateRequest;
import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.service.condition.WPEFService;

@RestController
@RequestMapping("/api/wpef")
public class WPEFController {
  public final WPEFService wpefService;

  public WPEFController(WPEFService service) {
    this.wpefService = service;
  }

  @GetMapping
  public List<WPEFResponse> getAll() {
    return wpefService.getAll().stream().map(WPEFResponse::from).toList();
  }

  @GetMapping("/{id}")
  public WPEFResponse getById(@PathVariable("id") Long id) {
    return WPEFResponse.from(wpefService.getById(id));
  }

  @PostMapping
  public ResponseEntity<WPEFResponse> create(@Valid @RequestBody WPEFCreateRequest request) {
    WPEF created = wpefService.create(
      request.condition(),
      request.phenomenon(),
      request.resolveFee(),
      request.vehicleIds()
    );

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}")
      .buildAndExpand(created.getId())
      .toUri();

    return ResponseEntity.created(location).body(WPEFResponse.from(created));
  }

  @PutMapping("/{id}")
  public WPEFResponse update(@PathVariable Long id, @Valid @RequestBody WPEFUpdateRequest request) {
    WPEF updated = wpefService.update(
      id,
      request.condition(),
      request.phenomenon(),
      request.resolveFee()
    );
    return WPEFResponse.from(updated);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    wpefService.delete(id);
  }
}
