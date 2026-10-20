package net.java.trial_task.application.repository.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.application.domain.entity.data.City;

/**
 * Data access for {@link City} entities.
 *
 * <p>The city name is treated as a natural key: at most one stored city is expected per
 * city name value.
 */
public interface CityRepository extends JpaRepository<City, Long> {

  /**
   * Finds the city with the given name.
   *
   * <p>Matching is by equality; case sensitivity follows the database comparison rules.
   * If more than one record matches, Spring Data throws
   * {@link org.springframework.dao.IncorrectResultSizeDataAccessException}.
   *
   * @param cityName city name, must not be {@code null}
   * @return the matching city, or {@link Optional#empty()} if none exists
   */
  Optional<City> getByCityName(String cityName);

  /**
   * Checks whether a city with the given name exists.
   *
   * <p>Matching rules are the same as in {@link #getByCityName(String)}.
   *
   * @param cityName city name, must not be {@code null}
   * @return {@code true} if at least one matching record exists, otherwise {@code false}
   */
  boolean existsByCityName(String cityName);
}
