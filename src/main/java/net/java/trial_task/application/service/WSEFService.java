package net.java.trial_task.application.service;

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
import net.java.trial_task.application.exceptions.VehicleNotFoundException;
import net.java.trial_task.application.exceptions.WSEFNotFoundException;
import net.java.trial_task.application.repository.VehicleRepository;
import net.java.trial_task.application.repository.WSEFRepository;

@Service
public class WSEFService {

  private final WSEFRepository wsefRepository;
  private final VehicleRepository vehicleRepository;

  public WSEFService(WSEFRepository wsefRepository, VehicleRepository vehicleRepository) {
    this.wsefRepository = wsefRepository;
    this.vehicleRepository = vehicleRepository;
  }

  @Transactional(readOnly = true)
  public List<WSEF> getAll() {
    return wsefRepository.findAll();
  }

  @Transactional(readOnly = true)
  public WSEF getById(Long id) {
    return wsefRepository.findById(id)
      .orElseThrow(() -> new WSEFNotFoundException(id));
  }

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

  @Transactional
  public void delete(Long id) {
    wsefRepository.delete(getById(id));
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
