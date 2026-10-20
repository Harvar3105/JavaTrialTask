package net.java.trial_task.unit.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.repository.condition.ATEFRepository;

@DataJpaTest
class ATEFRepositoryTests {
  @Autowired private ATEFRepository atefRepository;
  @Autowired private TestEntityManager entityManager;

  private Vehicle vehicle1;
  private Vehicle vehicle2;
  private ATEF atef1;
  private ATEF atef2;

  @BeforeEach
  void setUp() {
    vehicle1 = createVehicle("car");
    vehicle2 = createVehicle("bike");

    entityManager.persist(vehicle1);
    entityManager.persist(vehicle2);

    atef1 = createATEF(Set.of(vehicle1));
    atef2 = createATEF(Set.of(vehicle1, vehicle2));

    entityManager.persist(atef1);
    entityManager.persist(atef2);

    entityManager.flush();

    entityManager.clear();
  }

  @Test
  void findAll_shouldReturnATEFsWithVehicles() {
    List<ATEF> result = atefRepository.findAll();

    assertThat(result).hasSize(2);
    assertThat(result)
      .extracting(ATEF::getId)
      .containsExactlyInAnyOrder(atef1.getId(), atef2.getId());

    ATEF resultATEF = result.stream()
      .filter(a -> a.getId().equals(atef2.getId()))
      .findFirst()
      .orElseThrow();

    assertThat(resultATEF.getVehicles())
      .extracting(Vehicle::getId)
      .containsExactlyInAnyOrder(
        vehicle1.getId(),
        vehicle2.getId()
      );
  }

  @Test
  void findById_shouldReturnATEFWithVehicles() {
    Optional<ATEF> result = atefRepository.findById(atef1.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getVehicles())
      .extracting(Vehicle::getId)
      .containsExactly(vehicle1.getId());
  }

  @Test
  void findById_shouldReturnEmptyWhenATEFDoesNotExist() {
    Optional<ATEF> result = atefRepository.findById(-1L);

    assertThat(result).isEmpty();
  }

  @Test
  void findAllByVehicles_Id_shouldReturnMatchingATEFs() {
    List<ATEF> result =
      atefRepository.findAllByVehicles_Id(vehicle2.getId());

    assertThat(result)
      .extracting(ATEF::getId)
      .containsExactly(atef2.getId());

    assertThat(result.get(0).getVehicles())
      .extracting(Vehicle::getId)
      .containsExactlyInAnyOrder(
        vehicle1.getId(),
        vehicle2.getId()
      );
  }

  @Test
  void findAllByVehicles_Id_shouldReturnEmptyWhenNoATEFMatches() {
    List<ATEF> result =
      atefRepository.findAllByVehicles_Id(-1L);

    assertThat(result).isEmpty();
  }

  private ATEF createATEF(Set<Vehicle> vehicles) {
    return new ATEF(
      Condition.inRange,
      new BigDecimal("10.0"),
      new BigDecimal("20.0"),
      BigDecimal.ZERO,
      vehicles
    );
  }

  private Vehicle createVehicle(String type) {
    return new Vehicle(type);
  }
}
