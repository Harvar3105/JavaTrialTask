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
import net.java.trial_task.api.contracts.data.city.CityRequest;
import net.java.trial_task.api.contracts.data.city.CityResponse;
import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.service.data.CityService;

import org.springframework.web.bind.annotation.*;

/**
 * REST controller for {@link City} entities.
 *
 * <p>Base path: {@code /api/cities}. Request bodies are validated with Bean Validation before
 * the service is called; a body that fails validation is rejected with
 * {@link org.springframework.web.bind.MethodArgumentNotValidException} and the service is not
 * invoked. Exceptions thrown by {@link CityService} (for example {@link CityNotFoundException}
 * and {@link CityAlreadyExistsException}) are not handled in this class; their translation into
 * HTTP responses is determined by exception handlers outside this controller.
 */
@RestController
@RequestMapping("/api/cities")
public class CityController {
  private final CityService service;

  /**
   * Creates the controller.
   *
   * @param cityService service performing the city operations, must not be {@code null}
   */
  public CityController(@Autowired CityService cityService) {
    this.service = cityService;
  }

  /**
   * Handles {@code POST /api/cities}.
   *
   * <p>Creates a city and responds with status 201. The {@code Location} header contains the
   * URI of the created city, built from the current request URI and the generated id.
   *
   * @param request validated request containing the city name
   * @return the created city with status 201
   * @throws CityAlreadyExistsException if a city with the given name already exists
   */
  @PostMapping
  public ResponseEntity<CityResponse> create(@Valid @RequestBody CityRequest request) {
    City created = service.create(request.cityName());
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(created.getId()).toUri();
    return ResponseEntity.created(location).body(CityResponse.from(created));
  }

  /**
   * Handles {@code GET /api/cities}.
   *
   * <p>The order of the result is not defined.
   *
   * @return all stored cities, empty list if none exist
   */
  @GetMapping
  public List<CityResponse> findAll() {
    return service.findAll().stream().map(CityResponse::from).toList();
  }

  /**
   * Handles {@code GET /api/cities/{id}}.
   *
   * @param id id of the city
   * @return the city with the given id
   * @throws CityNotFoundException if no city with the given id exists
   */
  @GetMapping("/{id}")
  public CityResponse findById(@PathVariable("id") Long id) {
    return CityResponse.from(service.findById(id));
  }

  /**
   * Handles {@code PUT /api/cities/{id}}.
   *
   * <p>Replaces the name of the city. Uniqueness of the new name is not checked by the
   * controller or the service; see {@link CityService#update(Long, String)}.
   *
   * @param id id of the city to update
   * @param request validated request containing the new city name
   * @return the updated city with status 200
   * @throws CityNotFoundException if no city with the given id exists
   */
  @PutMapping("/{id}")
  public CityResponse update(@PathVariable("id") Long id, @Valid @RequestBody CityRequest request) {
    return CityResponse.from(service.update(id, request.cityName()));
  }

  /**
   * Handles {@code DELETE /api/cities/{id}}.
   *
   * <p>Deletes the city and responds with status 204 and no body. References to the city
   * from other records are not handled by the controller; see
   * {@link CityService#delete(Long)}.
   *
   * @param id id of the city to delete
   * @throws CityNotFoundException if no city with the given id exists
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") Long id) {
    service.delete(id);
  }
}
