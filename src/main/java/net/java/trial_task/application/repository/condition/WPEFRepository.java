package net.java.trial_task.application.repository.condition;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.application.domain.entity.condition.WPEF;

/**
 * Data access for {@link WPEF} (weather phenomenon extra fee) rules.
 *
 * <p>Each rule is associated with a set of vehicles to which it applies. The methods below
 * load that association together with the rule, so that {@link WPEF#getVehicles()} can be
 * accessed after the persistence context is closed.
 */
public interface WPEFRepository extends JpaRepository<WPEF, Long> {

  /**
   * Finds all rules with their vehicles initialized.
   *
   * <p>Overrides the inherited method only to apply the entity graph. The order of the
   * result is not defined.
   *
   * @return all stored rules, empty if none exist, never {@code null}
   */
  @Override
  @EntityGraph(attributePaths = "vehicles")
  List<WPEF> findAll();

  /**
   * Finds a rule by id with its vehicles initialized.
   *
   * <p>Overrides the inherited method only to apply the entity graph.
   *
   * @param id id of the rule, must not be {@code null}
   * @return the rule, or {@link Optional#empty()} if none exists
   */
  @Override
  @EntityGraph(attributePaths = "vehicles")
  Optional<WPEF> findById(Long id);

  /**
   * Finds all rules that apply to the given vehicle, with vehicles initialized.
   *
   * <p>A rule matches if its vehicle set contains the vehicle with the given id. The order
   * of the result is not defined.
   *
   * @param vehicleId id of the vehicle, must not be {@code null}
   * @return matching rules, empty if none exist, never {@code null}
   */
  @EntityGraph(attributePaths = "vehicles")
  List<WPEF> findAllByVehicles_Id(Long vehicleId);
}
