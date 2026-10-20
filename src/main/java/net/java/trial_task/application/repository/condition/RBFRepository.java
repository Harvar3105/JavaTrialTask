package net.java.trial_task.application.repository.condition;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.application.domain.entity.condition.RBF;

/**
 * Data access for {@link RBF} (regional base fee) entities.
 *
 * <p>A fee is defined per pair of city and vehicle type, referenced by their ids. At most
 * one record is expected per pair.
 */
public interface RBFRepository extends JpaRepository<RBF, Long> {

  /**
   * Finds the regional base fee for a city and vehicle type pair.
   *
   * <p>If more than one record matches, Spring Data throws
   * {@link org.springframework.dao.IncorrectResultSizeDataAccessException}. The ids are not
   * validated against existing cities and vehicles; a non-existent id yields an empty result.
   * A found record may have a {@code null} fee.
   *
   * @param cityId id of the city, must not be {@code null}
   * @param vehicleTypeId id of the vehicle, must not be {@code null}
   * @return the matching record, or {@link Optional#empty()} if none exists
   */
  Optional<RBF> findByCityIdAndVehicleTypeId(Long cityId, Long vehicleTypeId);

  /**
   * Checks whether a regional base fee exists for a city and vehicle type pair.
   *
   * @param cityId id of the city, must not be {@code null}
   * @param vehicleTypeId id of the vehicle, must not be {@code null}
   * @return {@code true} if at least one matching record exists, otherwise {@code false}
   */
  boolean existsByCityIdAndVehicleTypeId(Long cityId, Long vehicleTypeId);

  /**
   * Checks whether a regional base fee other than the one with the given id exists for a
   * city and vehicle type pair.
   *
   * <p>Intended for uniqueness checks where the record being checked must not conflict with
   * itself.
   *
   * @param cityId id of the city, must not be {@code null}
   * @param vehicleTypeId id of the vehicle, must not be {@code null}
   * @param id id of the record to exclude from the check, must not be {@code null}
   * @return {@code true} if a record with a different id and the same pair exists,
   *     otherwise {@code false}
   */
  boolean existsByCityIdAndVehicleTypeIdAndIdNot(Long cityId, Long vehicleTypeId, Long id);
}
