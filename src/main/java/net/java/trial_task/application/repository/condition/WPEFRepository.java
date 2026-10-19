package net.java.trial_task.application.repository.condition;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.application.domain.entity.condition.WPEF;

public interface WPEFRepository extends JpaRepository<WPEF, Long>{
  @Override
  @EntityGraph(attributePaths = "vehicles")
  List<WPEF> findAll();

  @Override
  @EntityGraph(attributePaths = "vehicles")
  Optional<WPEF> findById(Long id);

  @EntityGraph(attributePaths = "vehicles")
  List<WPEF> findAllByVehicles_Id(Long vehicleId);
}
