package net.java.trial_task.application.repository.data;

import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.application.domain.entity.data.Vehicle;

import java.util.Optional;


/**
 * Data access for {@link Vehicle} entities.
 *
 * <p>The vehicle type is treated as a natural key: at most one stored vehicle is expected
 * per vehicle type value.
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

  /**
   * Finds the vehicle with the given vehicle type.
   *
   * <p>Matching is by equality; case sensitivity follows the database comparison rules.
   * If more than one record matches, Spring Data throws
   * {@link org.springframework.dao.IncorrectResultSizeDataAccessException}.
   *
   * @param vehicleType vehicle type value, must not be {@code null}
   * @return the matching vehicle, or {@link Optional#empty()} if none exists
   */
  Optional<Vehicle> getByVehicleType(String vehicleType);

  /**
   * Checks whether a vehicle with the given vehicle type exists.
   *
   * <p>Matching rules are the same as in {@link #getByVehicleType(String)}.
   *
   * @param vehicleType vehicle type value, must not be {@code null}
   * @return {@code true} if at least one matching record exists, otherwise {@code false}
   */
  boolean existsByVehicleType(String vehicleType);

  /**
   * Checks whether a vehicle other than the one with the given id has the given vehicle type.
   *
   * <p>Intended for uniqueness checks on update, where the record being updated must not
   * conflict with itself. Matching rules for the vehicle type are the same as in
   * {@link #getByVehicleType(String)}.
   *
   * @param vehicleType vehicle type value, must not be {@code null}
   * @param id id of the record to exclude from the check, must not be {@code null}
   * @return {@code true} if a record with a different id and the same vehicle type exists,
   *     otherwise {@code false}
   */
  boolean existsByVehicleTypeAndIdNot(String vehicleType, Long id);
}
