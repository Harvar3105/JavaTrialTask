package net.java.trial_task.application.service.condition;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import net.java.trial_task.application.domain.entity.condition.RBF;
import net.java.trial_task.application.exceptions.condition.RBFNotFoundException;
import net.java.trial_task.application.repository.condition.RBFRepository;

/**
 * Business operations on {@link RBF} (regional base fee) records.
 *
 * <p>A record defines the base fee for a pair of city and vehicle type, referenced by their
 * ids. The pair is fixed at creation; only the fee can be changed afterwards. A {@code null}
 * fee means that usage of the vehicle in the city is forbidden.
 *
 * <p>Methods that fail to find a requested record throw {@link RBFNotFoundException}.
 */
@Service
public class RBFService {
  private final RBFRepository repository;

  /**
   * Creates the service.
   *
   * @param repository repository of regional base fees, must not be {@code null}
   */
  public RBFService(RBFRepository repository) {
    this.repository = repository;
  }

  /**
   * Returns all records.
   *
   * <p>No service-level transaction is opened; the call is executed within the transaction of
   * the repository method. The order of the result is not defined.
   *
   * @return all stored records, empty if none exist, never {@code null}
   */
  public List<RBF> findAll() {
    return repository.findAll();
  }

  /**
   * Returns the record with the given id.
   *
   * <p>No service-level transaction is opened; the call is executed within the transaction of
   * the repository method.
   *
   * @param id id of the record, must not be {@code null}
   * @return the record, never {@code null}
   * @throws RBFNotFoundException if no record with the given id exists
   */
  public RBF findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new RBFNotFoundException(id));
  }

  /**
   * Creates and stores a new record.
   *
   * <p>The record is flushed to the database before the method returns, so constraint
   * violations are reported by this call as persistence exceptions. The method does not check
   * that the city and vehicle exist and does not check for an existing record with the same
   * pair; such conditions are detected only by database constraints, if any are defined.
   *
   * @param cityId id of the city, must not be {@code null}
   * @param vehicleTypeId id of the vehicle, must not be {@code null}
   * @param fee base fee; {@code null} means that usage of the vehicle in the city is
   *     forbidden
   * @return the stored record, with generated id and timestamps
   */
  @Transactional
  public RBF create(Long cityId, Long vehicleTypeId, BigDecimal fee) {
    return repository.saveAndFlush(new RBF(cityId, vehicleTypeId, fee));
  }

  /**
   * Replaces the fee of an existing record.
   *
   * <p>The city and vehicle of the record are not changed. The change is persisted when the
   * transaction commits, so a persistence failure caused by the new value surfaces after this
   * method has returned, not inside it.
   *
   * @param id id of the record to update, must not be {@code null}
   * @param fee new base fee; {@code null} means that usage of the vehicle in the city is
   *     forbidden
   * @return the updated record
   * @throws RBFNotFoundException if no record with the given id exists
   */
  @Transactional
  public RBF updateFee(Long id, BigDecimal fee) {
    RBF entity = findById(id);
    entity.setFee(fee);
    return entity;
  }

  /**
   * Deletes the record with the given id.
   *
   * @param id id of the record to delete, must not be {@code null}
   * @throws RBFNotFoundException if no record with the given id exists
   */
  @Transactional
  public void delete(Long id) {
    repository.delete(findById(id));
  }
}
