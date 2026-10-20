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
  public void delete(@PathVariable("id") Long id) {
    wpefService.delete(id);
  }
}
