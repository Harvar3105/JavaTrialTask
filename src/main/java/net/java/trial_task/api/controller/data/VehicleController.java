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


/**
 * REST controller for {@link Vehicle} entities.
 *
 * <p>Base path: {@code /api/vehicles}. Request bodies are validated with Bean Validation
 * before the service is called; a body that fails validation is rejected with
 * {@link org.springframework.web.bind.MethodArgumentNotValidException} and the service is not
 * invoked. Exceptions thrown by {@link VehicleService} (for example
 * {@link VehicleNotFoundException} and {@link VehicleAlreadyExistsException}) are not handled
 * in this class; their translation into HTTP responses is determined by exception handlers
 * outside this controller.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

  private final VehicleService service;

  /**
   * Creates the controller.
   *
   * @param service service performing the vehicle operations, must not be {@code null}
   */
  public VehicleController(@Autowired VehicleService service) {
    this.service = service;
  }

  /**
   * Handles {@code GET /api/vehicles}.
   *
   * <p>The order of the result is not defined.
   *
   * @return all stored vehicles, empty list if none exist
   */
  @GetMapping
  public List<VehicleResponse> getAll() {
    return service.findAll().stream().map(VehicleResponse::from).toList();
  }

  /**
   * Handles {@code GET /api/vehicles/{id}}.
   *
   * @param id id of the vehicle
   * @return the vehicle with the given id
   * @throws VehicleNotFoundException if no vehicle with the given id exists
   */
  @GetMapping("/{id}")
  public VehicleResponse getById(@PathVariable("id") Long id) {
    return VehicleResponse.from(service.findById(id));
  }

  /**
   * Handles {@code POST /api/vehicles}.
   *
   * <p>Creates a vehicle and responds with status 201. The {@code Location} header contains
   * the URI of the created vehicle, built from the current request URI and the generated id.
   *
   * @param request validated request containing the vehicle type
   * @return the created vehicle with status 201
   * @throws VehicleAlreadyExistsException if a vehicle with the given type already exists
   */
  @PostMapping
  public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
    Vehicle created = service.create(request.vehicleType());
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(created.getId()).toUri();
    return ResponseEntity.created(location).body(VehicleResponse.from(created));
  }

  /**
   * Handles {@code PUT /api/vehicles/{id}}.
   *
   * <p>Replaces the vehicle type. Updating a vehicle with its current type is accepted.
   *
   * @param id id of the vehicle to update
   * @param request validated request containing the new vehicle type
   * @return the updated vehicle with status 200
   * @throws VehicleNotFoundException if no vehicle with the given id exists
   * @throws VehicleAlreadyExistsException if another vehicle already has the given type
   */
  @PutMapping("/{id}")
  public VehicleResponse update(@PathVariable("id") Long id, @Valid @RequestBody VehicleRequest request) {
    return VehicleResponse.from(service.update(id, request.vehicleType()));
  }

  /**
   * Handles {@code DELETE /api/vehicles/{id}}.
   *
   * <p>Deletes the vehicle and responds with status 204 and no body. References to the
   * vehicle from other records are not handled by the controller; see
   * {@link VehicleService#delete(Long)}.
   *
   * @param id id of the vehicle to delete
   * @throws VehicleNotFoundException if no vehicle with the given id exists
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") Long id) {
    service.delete(id);
  }
}
