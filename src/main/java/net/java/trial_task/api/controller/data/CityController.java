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
import net.java.trial_task.api.contracts.data.city.CityRequest;
import net.java.trial_task.api.contracts.data.city.CityResponse;
import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.exceptions.data.CityAlreadyExistsException;
import net.java.trial_task.application.exceptions.data.CityNotFoundException;
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
@Tag(name = "Cities", description = "Management of cities")
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
  @Operation(summary = "Create city", description = "Creates a city. City names are unique.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "City created",
      headers = @Header(name = "Location", description = "URI of the created city",
        schema = @Schema(type = "string", format = "uri"))),
    @ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content),
    @ApiResponse(responseCode = "409", description = "A city with the given name already exists", content = @Content)
  })
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
  @Operation(summary = "List cities", description = "Returns all cities. The order of the result is not defined.")
  @ApiResponse(responseCode = "200", description = "All stored cities; empty array if none exist")
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
  @Operation(summary = "Get city by id")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "City id", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "City found"),
    @ApiResponse(responseCode = "404", description = "No city with the given id", content = @Content)
  })
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
  @Operation(summary = "Replace city name",
    description = "Replaces the name of the city. Uniqueness of the new name is not checked on update.")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Id of the city to update", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "City updated"),
    @ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content),
    @ApiResponse(responseCode = "404", description = "No city with the given id", content = @Content)
  })
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
  @Operation(summary = "Delete city")
  @Parameter(name = "id", in = ParameterIn.PATH, description = "Id of the city to delete", example = "1")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "City deleted; no body", content = @Content),
    @ApiResponse(responseCode = "404", description = "No city with the given id", content = @Content)
  })
  public void delete(@PathVariable("id") Long id) {
    service.delete(id);
  }
}
