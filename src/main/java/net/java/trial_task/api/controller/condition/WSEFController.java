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
import net.java.trial_task.api.contracts.condition.wsef.WSEFCreateRequest;
import net.java.trial_task.api.contracts.condition.wsef.WSEFResponse;
import net.java.trial_task.api.contracts.condition.wsef.WSEFUpdateRequest;
import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.service.condition.WSEFService;

/**
 * REST controller for {@link WSEF} (wind speed extra fee) rules.
 *
 * <p>Base path: {@code /api/wsef}. Request bodies are validated with Bean Validation before
 * the service is called; a body that fails validation is rejected with
 * {@link org.springframework.web.bind.MethodArgumentNotValidException} and the service is not
 * invoked. Exceptions thrown by {@link WSEFService} (for example {@link WSEFNotFoundException}
 * and {@link VehicleNotFoundException}) are not handled in this class; their translation into
 * HTTP responses is determined by exception handlers outside this controller.
 *
 * <p>A {@code null} fee stored in a rule means that usage of the vehicle is forbidden.
 * The controller obtains the fee value from the request through its {@code resolveFee()}
 * method.
 */
@RestController
@RequestMapping("/api/wsef")
public class WSEFController {
  private final WSEFService wsefService;

  /**
   * Creates the controller.
   *
   * @param wsefService service performing the rule operations, must not be {@code null}
   */
  public WSEFController(WSEFService wsefService) {
    this.wsefService = wsefService;
  }

  /**
   * Handles {@code GET /api/wsef}.
   *
   * <p>Returns all rules with their vehicles. The order of the result is not defined.
   *
   * @return all stored rules, empty list if none exist
   */
  @GetMapping
  public List<WSEFResponse> getAll() {
    return wsefService.getAll().stream().map(WSEFResponse::from).toList();
  }

  /**
   * Handles {@code GET /api/wsef/{id}}.
   *
   * @param id id of the rule
   * @return the rule with the given id
   * @throws WSEFNotFoundException if no rule with the given id exists
   */
  @GetMapping("/{id}")
  public WSEFResponse getById(@PathVariable("id") Long id) {
    return WSEFResponse.from(wsefService.getById(id));
  }

  /**
   * Handles {@code POST /api/wsef}.
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

  /**
   * Handles {@code PUT /api/wsef/{id}}.
   *
   * <p>Replaces the condition, thresholds and fee of the rule. The vehicles of the rule are
   * not changed by this operation.
   *
   * @param id id of the rule to update
   * @param request validated new rule data
   * @return the updated rule with status 200
   * @throws WSEFNotFoundException if no rule with the given id exists
   */
  @PutMapping("/{id}")
  public WSEFResponse update(@PathVariable("id") Long id, @Valid @RequestBody WSEFUpdateRequest request) {
    WSEF updated = wsefService.update(
      id,
      request.condition(),
      request.windSpeed(),
      request.maxWindSpeed(),
      request.resolveFee()
    );
    return WSEFResponse.from(updated);
  }

  /**
   * Handles {@code DELETE /api/wsef/{id}}.
   *
   * <p>Deletes the rule and responds with status 204 and no body. Vehicles associated with
   * the rule are not deleted.
   *
   * @param id id of the rule to delete
   * @throws WSEFNotFoundException if no rule with the given id exists
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") Long id) {
    wsefService.delete(id);
  }
}
