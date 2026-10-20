package net.java.trial_task.application.service.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.exceptions.data.VehicleAlreadyExistsException;
import net.java.trial_task.application.exceptions.data.VehicleNotFoundException;
import net.java.trial_task.application.repository.data.VehicleRepository;

import java.util.List;


/**
 * Business operations on {@link Vehicle} entities.
 *
 * <p>The vehicle type is a unique value: the service rejects creation and renaming that would
 * produce two vehicles with the same type. The uniqueness check is performed by the service
 * before writing and is not atomic with the write; concurrent requests can pass the check
 * simultaneously, and only a database constraint, if one is defined, prevents duplicates in
 * that case.
 *
 * <p>Methods that fail to find a requested vehicle throw {@link VehicleNotFoundException}.
 */
@Service
public class VehicleService {
  private final VehicleRepository repository;

  /**
   * Creates the service.
   *
   * @param repository repository of vehicles, must not be {@code null}
   */
  public VehicleService(@Autowired VehicleRepository repository) {
    this.repository = repository;
  }

  /**
   * Returns all vehicles.
   *
   * <p>No service-level transaction is opened; the call is executed within the transaction of
   * the repository method. The order of the result is not defined.
   *
   * @return all stored vehicles, empty if none exist, never {@code null}
   */
  public List<Vehicle> findAll() {
    return repository.findAll();
  }

  /**
   * Returns the vehicle with the given id.
   *
   * <p>No service-level transaction is opened; the call is executed within the transaction of
   * the repository method.
   *
   * @param id id of the vehicle, must not be {@code null}
   * @return the vehicle, never {@code null}
   * @throws VehicleNotFoundException if no vehicle with the given id exists
   */
  public Vehicle findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new VehicleNotFoundException(id));
  }

  /**
   * Creates and stores a new vehicle.
   *
   * <p>The record is flushed to the database before the method returns, so constraint
   * violations are reported by this call as persistence exceptions.
   *
   * @param vehicleType vehicle type value, must not be {@code null}
   * @return the stored vehicle, with generated id and timestamps
   * @throws VehicleAlreadyExistsException if a vehicle with the given type already exists;
   *     matching follows the rules of {@link VehicleRepository#existsByVehicleType(String)}
   */
  @Transactional
  public Vehicle create(String vehicleType) {
    if (repository.existsByVehicleType(vehicleType)) throw new VehicleAlreadyExistsException(vehicleType);

    return repository.saveAndFlush(new Vehicle(vehicleType));
  }

  /**
   * Changes the vehicle type of an existing vehicle.
   *
   * <p>The vehicle itself is excluded from the uniqueness check, so updating a vehicle with
   * its current type does not fail. The change is flushed to the database before the method
   * returns.
   *
   * @param id id of the vehicle to update, must not be {@code null}
   * @param vehicleType new vehicle type value, must not be {@code null}
   * @return the updated vehicle
   * @throws VehicleNotFoundException if no vehicle with the given id exists
   * @throws VehicleAlreadyExistsException if another vehicle already has the given type
   */
  @Transactional
  public Vehicle update(Long id, String vehicleType) {
    Vehicle vehicle = findById(id);
    if (repository.existsByVehicleTypeAndIdNot(vehicleType, id)) throw new VehicleAlreadyExistsException(vehicleType);

    vehicle.setVehicleType(vehicleType);
    return repository.saveAndFlush(vehicle);
  }

  /**
   * Deletes the vehicle with the given id.
   *
   * <p>The method does not remove or check references to the vehicle from other records
   * (regional base fees, extra fee rules). If such references exist, the outcome is
   * determined by database constraints and entity mappings.
   *
   * @param id id of the vehicle to delete, must not be {@code null}
   * @throws VehicleNotFoundException if no vehicle with the given id exists
   */
  @Transactional
  public void delete(Long id) {
    repository.delete(findById(id));
  }
}
