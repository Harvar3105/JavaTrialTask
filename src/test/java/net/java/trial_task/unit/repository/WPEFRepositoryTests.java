package net.java.trial_task.unit.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.repository.WPEFRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WPEFRepositoryTests {
  @Autowired private WPEFRepository wpefRepository;
  @Autowired private TestEntityManager entityManager;

  private Vehicle vehicle1;
  private Vehicle vehicle2;
  private WPEF wpef1;
  private WPEF wpef2;

  @BeforeEach
  void setUp() {
      vehicle1 = new Vehicle("car");
      vehicle2 = new Vehicle("truck");

      entityManager.persist(vehicle1);
      entityManager.persist(vehicle2);

      wpef1 = createWPEF("rain", BigDecimal.ZERO, Set.of(vehicle1));
      wpef2 = createWPEF(
          "snow",
          new BigDecimal("5.0"),
          Set.of(vehicle1, vehicle2)
      );

      entityManager.persist(wpef1);
      entityManager.persist(wpef2);

      entityManager.flush();
      entityManager.clear();
  }

  @Test
  void findAll_shouldReturnAllWPEFsWithVehicles() {
    List<WPEF> result = wpefRepository.findAll();

    assertThat(result).hasSize(2);
    assertThat(result)
      .extracting(WPEF::getId)
      .containsExactlyInAnyOrder(wpef1.getId(), wpef2.getId());

    WPEF resultWPEF = result.stream()
      .filter(w -> w.getId().equals(wpef2.getId()))
      .findFirst()
      .orElseThrow();

    assertThat(resultWPEF.getVehicles())
      .extracting(Vehicle::getId)
      .containsExactlyInAnyOrder(
        vehicle1.getId(),
        vehicle2.getId()
      );
  }

  @Test
  void findById_shouldReturnWPEFWithVehicles() {
    Optional<WPEF> result = wpefRepository.findById(wpef1.getId());

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getVehicles())
      .extracting(Vehicle::getId)
      .containsExactly(vehicle1.getId());
  }

  @Test
  void findById_shouldReturnEmptyWhenWPEFDoesNotExist() {
    assertThat(wpefRepository.findById(-1L)).isEmpty();
  }

  @Test
  void findAllByVehicles_Id_shouldReturnMatchingWPEFs() {
    List<WPEF> result =
      wpefRepository.findAllByVehicles_Id(vehicle2.getId());

    assertThat(result)
      .extracting(WPEF::getId)
      .containsExactly(wpef2.getId());

    assertThat(result.get(0).getVehicles())
      .extracting(Vehicle::getId)
      .containsExactlyInAnyOrder(
        vehicle1.getId(),
        vehicle2.getId()
      );
  }

  @Test
  void findAllByVehicles_Id_shouldReturnEmptyWhenNoWPEFMatches() {
    assertThat(wpefRepository.findAllByVehicles_Id(-1L)).isEmpty();
  }

  private WPEF createWPEF(
    String phenomenon,
    BigDecimal fee,
    Set<Vehicle> vehicles
  ) {
    return new WPEF(
      Condition.related,
      phenomenon,
      fee,
      vehicles
    );
  }
}

