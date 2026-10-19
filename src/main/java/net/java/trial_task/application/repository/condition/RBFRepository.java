package net.java.trial_task.application.repository.condition;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.application.domain.entity.condition.RBF;

public interface RBFRepository extends JpaRepository<RBF, Long> {
  Optional<RBF> findByCityIdAndVehicleTypeId(Long cityId, Long vehicleTypeId);
  boolean existsByCityIdAndVehicleTypeId(Long cityId, Long vehicleTypeId);
  boolean existsByCityIdAndVehicleTypeIdAndIdNot(Long cityId, Long vehicleTypeId, Long id);
}
