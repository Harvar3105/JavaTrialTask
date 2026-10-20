package net.java.trial_task.application.service.condition;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.data.VehicleNotFoundException;
import net.java.trial_task.application.exceptions.condition.WSEFNotFoundException;
import net.java.trial_task.application.repository.data.VehicleRepository;
import net.java.trial_task.application.repository.condition.WSEFRepository;

/**
 * Business operations on {@link WSEF} (wind speed extra fee) rules.
 *
 * <p>Provides read, create, update and delete operations. A rule consists of a
 * {@link Condition}, a wind speed threshold, an optional upper threshold, a fee and the set
 * of vehicles to which the rule applies. A {@code null} fee means that usage of the vehicle
 * is forbidden under the rule's condition.
 *
 * <p>Each public method runs in its own transaction unless called from an already active
 * one. Methods that fail to find a requested rule throw {@link WSEFNotFoundException}.
 */
@Service
public class WSEFService {

  private final WSEFRepository wsefRepository;
  private final VehicleRepository vehicleRepository;

  /**
   * Creates the service.
   *
   * @param wsefRepository repository of wind speed rules, must not be {@code null}
   * @param vehicleRepository repository used to resolve vehicle ids, must not be {@code null}
   */
  public WSEFService(WSEFRepository wsefRepository, VehicleRepository vehicleRepository) {
    this.wsefRepository = wsefRepository;
    this.vehicleRepository = vehicleRepository;
  }

  /**
   * Returns all rules with their vehicles.
   *
   * <p>Runs in a read-only transaction. The order of the result is not defined.
   *
   * @return all stored rules, empty if none exist, never {@code null}
   */
  @Transactional(readOnly = true)
  public List<WSEF> getAll() {
    return wsefRepository.findAll();
  }

  /**
   * Returns the rule with the given id, with its vehicles.
   *
   * <p>Runs in a read-only transaction.
   *
   * @param id id of the rule, must not be {@code null}
   * @return the rule, never {@code null}
   * @throws WSEFNotFoundException if no rule with the given id exists
   */
  @Transactional(readOnly = true)
  public WSEF getById(Long id) {
    return wsefRepository.findById(id)
      .orElseThrow(() -> new WSEFNotFoundException(id));
  }

  /**
   * Creates and stores a new rule.
   *
   * <p>The vehicles are resolved by id before the rule is created. If {@code vehicleIds} is
   * {@code null} or empty, the rule is created without vehicles. The rule's own fields are
   * passed to the {@link WSEF} constructor without additional checks in this method.
   *
   * @param condition comparison condition of the rule
   * @param windSpeed wind speed threshold
   * @param maxWindSpeed upper threshold used by range conditions, may be {@code null}
   * @param fee extra fee; {@code null} means that usage of the vehicle is forbidden
   * @param vehicleIds ids of the vehicles to which the rule applies, may be {@code null}
   * @return the stored rule, with generated id and timestamps
   * @throws VehicleNotFoundException if at least one of the given vehicle ids does not exist;
   *     the message lists the missing ids and nothing is stored
   */
  @Transactional
  public WSEF create(
    Condition condition,
    BigDecimal windSpeed,
    BigDecimal maxWindSpeed,
    BigDecimal fee,
    Set<Long> vehicleIds
  ) {
    Set<Vehicle> vehicles = resolveVehicles(vehicleIds);
    return wsefRepository.save(new WSEF(condition, windSpeed, maxWindSpeed, fee, vehicles));
  }

  /**
   * Replaces the condition, thresholds and fee of an existing rule.
   *
   * <p>The vehicle set of the rule is not changed. The changes are persisted when the
   * transaction commits, so a persistence failure caused by the new values surfaces after
   * this method has returned, not inside it.
   *
   * @param id id of the rule to update, must not be {@code null}
   * @param condition new comparison condition
   * @param windSpeed new wind speed threshold
   * @param maxWindSpeed new upper threshold used by range conditions, may be {@code null}
   * @param fee new extra fee; {@code null} means that usage of the vehicle is forbidden
   * @return the updated rule
   * @throws WSEFNotFoundException if no rule with the given id exists
   */
  @Transactional
  public WSEF update(
    Long id,
    Condition condition,
    BigDecimal windSpeed,
    BigDecimal maxWindSpeed,
    BigDecimal fee
  ) {
    WSEF wsef = getById(id);
    wsef.update(condition, windSpeed, maxWindSpeed, fee);
    return wsef;
  }

  /**
   * Deletes the rule with the given id.
   *
   * <p>Vehicles associated with the rule are not deleted.
   *
   * @param id id of the rule to delete, must not be {@code null}
   * @throws WSEFNotFoundException if no rule with the given id exists
   */
  @Transactional
  public void delete(Long id) {
    wsefRepository.delete(getById(id));
  }

  /**
   * Loads the vehicles with the given ids.
   *
   * @param vehicleIds ids to resolve, may be {@code null} or empty
   * @return a new modifiable set of the found vehicles; empty if {@code vehicleIds} is
   *     {@code null} or empty
   * @throws VehicleNotFoundException if at least one id has no matching vehicle; the message
   *     lists all missing ids
   */
  private Set<Vehicle> resolveVehicles(Set<Long> vehicleIds) {
    if (vehicleIds == null || vehicleIds.isEmpty()) return new HashSet<>();

    List<Vehicle> found = vehicleRepository.findAllById(vehicleIds);
    if (found.size() != vehicleIds.size()) {
      Set<Long> foundIds = found.stream().map(Vehicle::getId).collect(Collectors.toSet());
      Set<Long> missing = new HashSet<>(vehicleIds);
      missing.removeAll(foundIds);
      throw new VehicleNotFoundException("Vehicles not found: " + missing);
    }
    return new HashSet<>(found);
  }
}
