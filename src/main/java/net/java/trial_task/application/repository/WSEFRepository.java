package net.java.trial_task.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.application.domain.entity.condition.WSEF;

public interface WSEFRepository extends JpaRepository<WSEF, Long> {
  @Override
  @EntityGraph(attributePaths = "vehicles")
  List<WSEF> findAll();

  @Override
  @EntityGraph(attributePaths = "vehicles")
  Optional<WSEF> findById(Long id);

  @EntityGraph(attributePaths = "vehicles")
  List<WSEF> findAllByVehicles_Id(Long vehicleId);
}
