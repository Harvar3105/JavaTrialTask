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
import net.java.trial_task.api.contracts.condition.wpef.WPEFCreateRequest;
import net.java.trial_task.api.contracts.condition.wpef.WPEFResponse;
import net.java.trial_task.api.contracts.condition.wpef.WPEFUpdateRequest;
import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.exceptions.condition.WPEFNotFoundException;
import net.java.trial_task.application.exceptions.data.VehicleNotFoundException;
import net.java.trial_task.application.service.condition.WPEFService;

/**
 * REST controller for {@link WPEF} (weather phenomenon extra fee) rules.
 *
 * <p>Base path: {@code /api/wpef}. Request bodies are validated with Bean Validation before
 * the service is called; a body that fails validation is rejected with
 * {@link org.springframework.web.bind.MethodArgumentNotValidException} and the service is not
 * invoked. Exceptions thrown by {@link WPEFService} (for example {@link WPEFNotFoundException}
 * and {@link VehicleNotFoundException}) are not handled in this class; their translation into
 * HTTP responses is determined by exception handlers outside this controller.
 *
 * <p>A {@code null} fee stored in a rule means that usage of the vehicle is forbidden.
 * The controller obtains the fee value from the request through its {@code resolveFee()}
 * method.
 */
@RestController
@RequestMapping("/api/wpef")
@Tag(name = "Weather phenomenon extra fee (WPEF)",
  description = "Rules for extra fees based on the weather phenomenon. A null fee (forbidden = true) means usage of the vehicle is forbidden.")
public class WPEFController {
  public final WPEFService wpefService;

  /**
   * Creates the controller.
   *
   * @param service service performing the rule operations, must not be {@code null}
   */
  public WPEFController(WPEFService service) {
    this.wpefService = service;
  }

  /**
   * Handles {@code GET /api/wpef}.
   *
   * <p>Returns all rules with their vehicles. The order of the result is not defined.
   *
   * @return all stored rules, empty list if none exist
   */
  @GetMapping
  @Operation(summary = "List WPEF rules",
    description = "Returns all rules with their vehicles. The order of the result is not defined.")
  @ApiResponse(responseCode = "200", description = "All stored rules; empty array if none exist")
  public List<WPEFResponse> getAll() {
    return wpefService.getAll().stream().map(WPEFResponse::from).toList();
  }

  /**
   * Handles {@code GET /api/wpef/{id}}.
   *
   * @param id id of the rule
   * @return the rule with the given id
   * @throws WPEFNotFoundException if no rule with the given id exists
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get WPEF rule by id")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Rule id", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Rule found"),
    @ApiResponse(responseCode = "404", description = "No rule with the given id", content = @Content)
  })
  public WPEFResponse getById(@PathVariable("id") Long id) {
    return WPEFResponse.from(wpefService.getById(id));
  }

  /**
   * Handles {@code POST /api/wpef}.
   *
   * <p>Creates a rule and responds with status 201. The {@code Location} header contains the
   * URI of the created rule, built from the current request URI and the generated id.
   *
   * @param request validated rule data, including the ids of the vehicles to which the rule
   *     applies
   * @return the created rule with status 201
   * @throws VehicleNotFoundException if at least one of the vehicle ids does not exist
   */
  @PostMapping
  @Operation(summary = "Create WPEF rule",
    description = "Creates a rule applying to the vehicles listed in 'vehicleIds'. If 'forbidden' is true, 'fee' must be null; otherwise 'fee' is required.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Rule created",
      headers = @Header(name = "Location", description = "URI of the created rule",
        schema = @Schema(type = "string", format = "uri"))),
    @ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content),
    @ApiResponse(responseCode = "404", description = "At least one of the vehicle ids does not exist", content = @Content)
  })
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

  /**
   * Handles {@code PUT /api/wpef/{id}}.
   *
   * <p>Replaces the condition, phenomenon and fee of the rule. The vehicles of the rule are
   * not changed by this operation.
   *
   * @param id id of the rule to update
   * @param request validated new rule data
   * @return the updated rule with status 200
   * @throws WPEFNotFoundException if no rule with the given id exists
   */
  @PutMapping("/{id}")
  @Operation(summary = "Replace WPEF rule",
    description = "Replaces the condition, phenomenon and fee. The vehicles of the rule are not changed by this operation.")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Id of the rule to update", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Rule updated"),
    @ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content),
    @ApiResponse(responseCode = "404", description = "No rule with the given id", content = @Content)
  })
  public WPEFResponse update(@PathVariable("id") Long id, @Valid @RequestBody WPEFUpdateRequest request) {
    WPEF updated = wpefService.update(
      id,
      request.condition(),
      request.phenomenon(),
      request.resolveFee()
    );
    return WPEFResponse.from(updated);
  }

  /**
   * Handles {@code DELETE /api/wpef/{id}}.
   *
   * <p>Deletes the rule and responds with status 204 and no body. Vehicles associated with
   * the rule are not deleted.
   *
   * @param id id of the rule to delete
   * @throws WPEFNotFoundException if no rule with the given id exists
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete WPEF rule",
    description = "Deletes the rule. Vehicles associated with the rule are not deleted.")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Id of the rule to delete", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Rule deleted; no body", content = @Content),
    @ApiResponse(responseCode = "404", description = "No rule with the given id", content = @Content)
  })
  public void delete(@PathVariable("id") Long id) {
    wpefService.delete(id);
  }
}
