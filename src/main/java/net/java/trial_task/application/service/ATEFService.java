package net.java.trial_task.application.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.ATEFNotFoundException;
import net.java.trial_task.application.exceptions.VehicleNotFoundException;
import net.java.trial_task.application.repository.ATEFRepository;
import net.java.trial_task.application.repository.VehicleRepository;

@Service
public class ATEFService {

  private final ATEFRepository atefRepository;
  private final VehicleRepository vehicleRepository;

  public ATEFService(ATEFRepository atefRepository, VehicleRepository vehicleRepository) {
    this.atefRepository = atefRepository;
    this.vehicleRepository = vehicleRepository;
  }

  @Transactional(readOnly = true)
  public List<ATEF> getAll() {
    return atefRepository.findAll();
  }

  @Transactional(readOnly = true)
  public ATEF getById(Long id) {
    return atefRepository.findById(id)
      .orElseThrow(() -> new ATEFNotFoundException(id));
  }

  @Transactional
  public ATEF create(
    Condition condition,
    BigDecimal temperature,
    BigDecimal maxTemperature,
    BigDecimal fee,
    Set<Long> vehicleIds
  ) {
    Set<Vehicle> vehicles = resolveVehicles(vehicleIds);
    return atefRepository.save(new ATEF(condition, temperature, maxTemperature, fee, vehicles));
  }

  @Transactional
  public ATEF update(
    Long id,
    Condition condition,
    BigDecimal temperature,
    BigDecimal maxTemperature,
    BigDecimal fee
  ) {
    ATEF atef = getById(id);
    atef.update(condition, temperature, maxTemperature, fee);
    return atef;
  }

  @Transactional
  public void delete(Long id) {
    atefRepository.delete(getById(id));
  }

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
