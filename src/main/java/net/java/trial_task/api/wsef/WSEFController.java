package net.java.trial_task.api.wsef;

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
import net.java.trial_task.api.contracts.wsef.WSEFCreateRequest;
import net.java.trial_task.api.contracts.wsef.WSEFResponse;
import net.java.trial_task.api.contracts.wsef.WSEFUpdateRequest;
import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.service.WSEFService;

@RestController
@RequestMapping("/api/wsef")
public class WSEFController {
  private final WSEFService wsefService;

  public WSEFController(WSEFService wsefService) {
    this.wsefService = wsefService;
  }

  @GetMapping
  public List<WSEFResponse> getAll() {
    return wsefService.getAll().stream().map(WSEFResponse::from).toList();
  }

  @GetMapping("/{id}")
  public WSEFResponse getById(@PathVariable("id") Long id) {
    return WSEFResponse.from(wsefService.getById(id));
  }

  @PostMapping
  public ResponseEntity<WSEFResponse> create(@Valid @RequestBody WSEFCreateRequest request) {
    WSEF created = wsefService.create(
      request.condition(),
      request.windSpeed(),
      request.maxWindSpeed(),
      request.resolveFee(),
      request.vehicleIds()
    );

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}")
      .buildAndExpand(created.getId())
      .toUri();

    return ResponseEntity.created(location).body(WSEFResponse.from(created));
  }

  @PutMapping("/{id}")
  public WSEFResponse update(@PathVariable Long id, @Valid @RequestBody WSEFUpdateRequest request) {
    WSEF updated = wsefService.update(
      id,
      request.condition(),
      request.windSpeed(),
      request.maxWindSpeed(),
      request.resolveFee()
    );
    return WSEFResponse.from(updated);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    wsefService.delete(id);
  }
}
