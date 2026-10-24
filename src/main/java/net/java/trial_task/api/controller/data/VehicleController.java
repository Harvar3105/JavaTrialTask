package net.java.trial_task.api.controller.data;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.headers.Header;
import jakarta.validation.Valid;
import net.java.trial_task.api.contracts.data.vehicle.VehicleRequest;
import net.java.trial_task.api.contracts.data.vehicle.VehicleResponse;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.exceptions.data.VehicleAlreadyExistsException;
import net.java.trial_task.application.exceptions.data.VehicleNotFoundException;
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
@Tag(name = "Vehicles", description = "Management of vehicle types")
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
  @Operation(
    summary = "List vehicles",
    description = "Returns all vehicles. The order of the result is not defined.")
  @ApiResponse(
    responseCode = "200",
    description = "All stored vehicles; empty array if none exist")
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
  @Operation(summary = "Get vehicle by id")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Vehicle id", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Vehicle found"),
    @ApiResponse(responseCode = "404", description = "No vehicle with the given id", content = @Content)
  })
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
  @Operation(summary = "Create vehicle", description = "Creates a vehicle type. Vehicle type names are unique.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Vehicle created",
      headers = @Header(name = "Location", description = "URI of the created vehicle",
        schema = @Schema(type = "string", format = "uri"))),
    @ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content),
    @ApiResponse(responseCode = "409", description = "A vehicle with the given type already exists", content = @Content)
  })
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
  @Operation(summary = "Replace vehicle type",
    description = "Replaces the vehicle type. Updating a vehicle with its current type is accepted.")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Id of the vehicle to update", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Vehicle updated"),
    @ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content),
    @ApiResponse(responseCode = "404", description = "No vehicle with the given id", content = @Content),
    @ApiResponse(responseCode = "409", description = "Another vehicle already has the given type", content = @Content)
  })
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
  @Operation(summary = "Delete vehicle")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Id of the vehicle to delete", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Vehicle deleted; no body", content = @Content),
    @ApiResponse(responseCode = "404", description = "No vehicle with the given id", content = @Content)
  })
  public void delete(@PathVariable("id") Long id) {
    service.delete(id);
  }
}
