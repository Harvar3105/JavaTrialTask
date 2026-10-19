package net.java.trial_task.application.repository.condition;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.application.domain.entity.condition.ATEF;

public interface ATEFRepository extends JpaRepository<ATEF, Long> {
  @Override
  @EntityGraph(attributePaths = "vehicles")
  List<ATEF> findAll();

  @Override
  @EntityGraph(attributePaths = "vehicles")
  Optional<ATEF> findById(Long id);

  @EntityGraph(attributePaths = "vehicles")
  List<ATEF> findAllByVehicles_Id(Long vehicleId);
}
