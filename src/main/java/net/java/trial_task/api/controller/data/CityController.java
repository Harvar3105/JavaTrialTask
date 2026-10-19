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

@RestController
@RequestMapping("/api/cities")
public class CityController {
  private final CityService service;

  public CityController(@Autowired CityService cityService) {
    this.service = cityService;
  }

  @PostMapping
  public ResponseEntity<CityResponse> create(@Valid @RequestBody CityRequest request) {
    City created = service.create(request.cityName());
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
      .path("/{id}").buildAndExpand(created.getId()).toUri();
    return ResponseEntity.created(location).body(CityResponse.from(created));
  }

  @GetMapping
  public List<CityResponse> findAll() {
    return service.findAll().stream().map(CityResponse::from).toList();
  }

  @GetMapping("/{id}")
  public CityResponse findById(@PathVariable("id") Long id) {
    return CityResponse.from(service.findById(id));
  }

  @PutMapping("/{id}")
  public CityResponse update(@PathVariable("id") Long id, @Valid @RequestBody CityRequest request) {
    return CityResponse.from(service.update(id, request.cityName()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") Long id) {
    service.delete(id);
  }
}