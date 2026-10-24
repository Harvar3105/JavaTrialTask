package net.java.trial_task.api.controller.condition;

import java.math.BigDecimal;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.headers.Header;

import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import net.java.trial_task.api.contracts.condition.rbf.RBFCreateRequest;
import net.java.trial_task.api.contracts.condition.rbf.RBFResponse;
import net.java.trial_task.api.contracts.condition.rbf.RBFUpdateRequest;
import net.java.trial_task.application.domain.entity.condition.RBF;
import net.java.trial_task.application.exceptions.condition.RBFNotFoundException;
import net.java.trial_task.application.service.condition.RBFService;

/**
 * REST controller for {@link RBF} (regional base fee) records.
 *
 * <p>Base path: {@code /api/rbf}. A record defines the base fee for a pair of city and
 * vehicle type, referenced by ids. The pair is fixed at creation; only the fee can be
 * changed afterwards. A {@code null} fee means that usage of the vehicle in the city is
 * forbidden.
 *
 * <p>Request bodies are validated with Bean Validation before the service is called; a body
 * that fails validation is rejected with
 * {@link org.springframework.web.bind.MethodArgumentNotValidException} and the service is not
 * invoked. Exceptions thrown by {@link RBFService} (for example {@link RBFNotFoundException})
 * are not handled in this class; their translation into HTTP responses is determined by
 * exception handlers outside this controller.
 */
@RestController
@RequestMapping("/api/rbf")
@Tag(name = "Regional base fee (RBF)",
  description = "Base fee for a pair of city and vehicle type. The pair is fixed at creation; only the fee can be changed. A null fee means usage of the vehicle in the city is forbidden.")
public class RBFController {
  private final RBFService service;

  /**
   * Creates the controller.
   *
   * @param service service performing the record operations, must not be {@code null}
   */
  public RBFController(RBFService service) {
    this.service = service;
  }

  /**
   * Handles {@code GET /api/rbf}.
   *
   * <p>The order of the result is not defined.
   *
   * @return all stored records, empty list if none exist
   */
  @GetMapping
  @Operation(summary = "List RBF records", description = "Returns all records. The order of the result is not defined.")
  @ApiResponse(responseCode = "200", description = "All stored records; empty array if none exist")
  public List<RBFResponse> findAll() {
    return service.findAll().stream().map(RBFResponse::from).toList();
  }

  /**
   * Handles {@code GET /api/rbf/{id}}.
   *
   * @param id id of the record
   * @return the record with the given id
   * @throws RBFNotFoundException if no record with the given id exists
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get RBF record by id")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Record id", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Record found"),
    @ApiResponse(responseCode = "404", description = "No record with the given id", content = @Content)
  })
  public RBFResponse findById(@PathVariable("id") Long id) {
    return RBFResponse.from(service.findById(id));
  }

  /**
   * Handles {@code POST /api/rbf}.
   *
   * <p>Creates a record and responds with status 201. The {@code Location} header contains
   * the URI of the created record, built from the current request URI and the generated id.
   * Existence of the referenced city and vehicle and uniqueness of the pair are not checked
   * by the controller or the service; see {@link RBFService#create(Long, Long, BigDecimal)}.
   *
   * @param request validated record data: city id, vehicle id and fee
   * @return the created record with status 201
   */
  @PostMapping
  @Operation(summary = "Create RBF record",
    description = "Creates a record for a city and vehicle type referenced by ids. Existence of the referenced city and vehicle and uniqueness of the pair are not checked.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Record created",
      headers = @Header(name = "Location", description = "URI of the created record",
        schema = @Schema(type = "string", format = "uri"))),
    @ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content)
  })
  public ResponseEntity<RBFResponse> create(@Valid @RequestBody RBFCreateRequest request) {
    RBF created = service.create(request.cityId(), request.vehicleTypeId(), request.fee());
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(created.getId()).toUri();
    return ResponseEntity.created(location).body(RBFResponse.from(created));
  }

  /**
   * Handles {@code PUT /api/rbf/{id}}.
   *
   * <p>Replaces only the fee of the record; the city and vehicle of the record cannot be
   * changed.
   *
   * @param id id of the record to update
   * @param request validated request containing the new fee
   * @return the updated record with status 200
   * @throws RBFNotFoundException if no record with the given id exists
   */
  @PutMapping("/{id}")
  @Operation(summary = "Replace RBF fee",
    description = "Replaces only the fee. The city and vehicle of the record cannot be changed. A null fee means usage is forbidden.")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Id of the record to update", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Record updated"),
    @ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content),
    @ApiResponse(responseCode = "404", description = "No record with the given id", content = @Content)
  })
  public RBFResponse updateFee(
    @PathVariable("id") Long id,
    @Valid @RequestBody RBFUpdateRequest request
  ) {
    return RBFResponse.from(service.updateFee(id, request.fee()));
  }

  /**
   * Handles {@code DELETE /api/rbf/{id}}.
   *
   * <p>Deletes the record and responds with status 204 and no body.
   *
   * @param id id of the record to delete
   * @throws RBFNotFoundException if no record with the given id exists
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete RBF record")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Id of the record to delete", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Record deleted; no body", content = @Content),
    @ApiResponse(responseCode = "404", description = "No record with the given id", content = @Content)
  })
  public void delete(@PathVariable("id") Long id) {
    service.delete(id);
  }
}