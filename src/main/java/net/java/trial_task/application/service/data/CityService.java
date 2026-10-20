package net.java.trial_task.application.service.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.exceptions.data.CityAlreadyExistsException;
import net.java.trial_task.application.exceptions.data.CityNotFoundException;
import net.java.trial_task.application.repository.data.CityRepository;

/**
 * Business operations on {@link City} entities.
 *
 * <p>The city name is intended to be unique. Creation checks this before writing; renaming
 * does not (see {@link #update(Long, String)}). The check on creation is not atomic with the
 * write; concurrent requests can pass it simultaneously, and only a database constraint, if
 * one is defined, prevents duplicates in that case.
 *
 * <p>Methods that fail to find a requested city throw {@link CityNotFoundException}.
 */
@Service
public class CityService {
  private final CityRepository repository;

  /**
   * Creates the service.
   *
   * @param cityRepository repository of cities, must not be {@code null}
   */
  public CityService(@Autowired CityRepository cityRepository) {
    this.repository = cityRepository;
  }

  /**
   * Creates and stores a new city.
   *
   * <p>The record is flushed to the database before the method returns, so constraint
   * violations are reported by this call as persistence exceptions.
   *
   * @param cityName name of the city, must not be {@code null}
   * @return the stored city, with generated id and timestamps
   * @throws CityAlreadyExistsException if a city with the given name already exists;
   *     matching follows the rules of {@link CityRepository#existsByCityName(String)}
   */
  @Transactional
  public City create(String cityName) {
    if (repository.existsByCityName(cityName)) throw new CityAlreadyExistsException(cityName);

    return repository.saveAndFlush(new City(cityName));
  }

  /**
   * Returns all cities.
   *
   * <p>No service-level transaction is opened; the call is executed within the transaction of
   * the repository method. The order of the result is not defined.
   *
   * @return all stored cities, empty if none exist, never {@code null}
   */
  public List<City> findAll() {
    return repository.findAll();
  }

  /**
   * Returns the city with the given id.
   *
   * <p>No service-level transaction is opened; the call is executed within the transaction of
   * the repository method.
   *
   * @param id id of the city, must not be {@code null}
   * @return the city, never {@code null}
   * @throws CityNotFoundException if no city with the given id exists
   */
  public City findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new CityNotFoundException(id));
  }

  /**
   * Changes the name of an existing city.
   *
   * <p>The method does not check whether another city already has the given name. The change
   * is persisted when the transaction commits, so a persistence failure (for example a
   * unique constraint violation, if one is defined) surfaces after this method has returned,
   * not inside it.
   *
   * @param id id of the city to update, must not be {@code null}
   * @param cityName new name of the city, must not be {@code null}
   * @return the updated city
   * @throws CityNotFoundException if no city with the given id exists
   */
  @Transactional
  public City update(Long id, String cityName) {
    City city = findById(id);
    city.setCityName(cityName);
    return city;
  }

  /**
   * Deletes the city with the given id.
   *
   * <p>The method does not remove or check references to the city from other records
   * (regional base fees). If such references exist, the outcome is determined by database
   * constraints and entity mappings.
   *
   * @param id id of the city to delete, must not be {@code null}
   * @throws CityNotFoundException if no city with the given id exists
   */
  @Transactional
  public void delete(Long id) {
    repository.delete(findById(id));
  }
}
