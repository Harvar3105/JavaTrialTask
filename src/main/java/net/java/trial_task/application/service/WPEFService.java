package net.java.trial_task.application.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.VehicleNotFoundException;
import net.java.trial_task.application.exceptions.WPEFNotFoundException;
import net.java.trial_task.application.repository.VehicleRepository;
import net.java.trial_task.application.repository.WPEFRepository;

@Service 
public class WPEFService {
  private final WPEFRepository wpefRepo;
  private final VehicleRepository vehicleRepository;

    public WPEFService(WPEFRepository atefRepository, VehicleRepository vehicleRepository) {
    this.wpefRepo = atefRepository;
    this.vehicleRepository = vehicleRepository;
  }

  @Transactional(readOnly = true)
  public List<WPEF> getAll() {
    return wpefRepo.findAll();
  }

  @Transactional(readOnly = true)
  public WPEF getById(Long id) {
    return wpefRepo.findById(id)
      .orElseThrow(() -> new WPEFNotFoundException(id));
  }

  @Transactional
  public WPEF create(
    Condition condition,
    String phenomenon,
    BigDecimal fee,
    Set<Long> vehicleIds
  ) {
    Set<Vehicle> vehicles = resolveVehicles(vehicleIds);
    return wpefRepo.save(new WPEF(condition, phenomenon, fee, vehicles));
  }

  @Transactional
  public WPEF update(
    Long id,
    Condition condition,
    String phenomenon,
    BigDecimal fee
  ) {
    WPEF wpef = getById(id);
    wpef.update(condition, phenomenon, fee);
    return wpef;
  }

  @Transactional
  public void delete(Long id) {
    wpefRepo.delete(getById(id));
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
