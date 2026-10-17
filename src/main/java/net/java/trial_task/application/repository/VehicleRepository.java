package net.java.trial_task.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.shared.domain.entity.data.Vehicle;
import java.util.Optional;


public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
  Optional<Vehicle> getByVehicleType(String vehicleType);
  boolean existsByVehicleType(String vehicleType);
  boolean existsByVehicleTypeAndIdNot(String vehicleType, Long id);
}
