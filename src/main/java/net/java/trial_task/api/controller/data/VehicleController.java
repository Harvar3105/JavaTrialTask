package net.java.trial_task.api.controller.data;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import net.java.trial_task.api.contracts.data.vehicle.VehicleRequest;
import net.java.trial_task.api.contracts.data.vehicle.VehicleResponse;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.service.data.VehicleService;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

  private final VehicleService service;

  public VehicleController(@Autowired VehicleService service) {
    this.service = service;
  }

  @GetMapping
  public List<VehicleResponse> getAll() {
    return service.findAll().stream().map(VehicleResponse::from).toList();
  }

  @GetMapping("/{id}")
  public VehicleResponse getById(@PathVariable("id") Long id) {
    return VehicleResponse.from(service.findById(id));
  }

  @PostMapping
  public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
    Vehicle created = service.create(request.vehicleType());
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(created.getId()).toUri();
    return ResponseEntity.created(location).body(VehicleResponse.from(created));
  }

  @PutMapping("/{id}")
  public VehicleResponse update(@PathVariable("id") Long id, @Valid @RequestBody VehicleRequest request) {
    return VehicleResponse.from(service.update(id, request.vehicleType()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") Long id) {
    service.delete(id);
  }
}
