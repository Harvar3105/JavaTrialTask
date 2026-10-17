package net.java.trial_task.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.application.exceptions.VehicleAlreadyExistsException;
import net.java.trial_task.application.exceptions.VehicleNotFoundException;
import net.java.trial_task.application.repository.VehicleRepository;
import net.java.trial_task.shared.domain.entity.data.Vehicle;

import java.util.List;


@Service
public class VehicleService {
  private final VehicleRepository repository;

  public VehicleService(@Autowired VehicleRepository repository) {
    this.repository = repository;
  }

  public List<Vehicle> findAll() {
    return repository.findAll();
  }

  public Vehicle findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new VehicleNotFoundException(id));
  }

  @Transactional
  public Vehicle create(String vehicleType) {
    if (repository.existsByVehicleType(vehicleType)) throw new VehicleAlreadyExistsException(vehicleType);
    
    return repository.saveAndFlush(new Vehicle(vehicleType));
  }

  @Transactional
  public Vehicle update(Long id, String vehicleType) {
    Vehicle vehicle = findById(id);
    if (repository.existsByVehicleTypeAndIdNot(vehicleType, id)) throw new VehicleAlreadyExistsException(vehicleType);

    vehicle.setVehicleType(vehicleType);
    return repository.saveAndFlush(vehicle);
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(findById(id));
  }
}