package net.java.trial_task.unit.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.repository.WSEFRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WSEFRepositoryTests {
  @Autowired private WSEFRepository wsefRepository;
  @Autowired private TestEntityManager entityManager;

  private Vehicle vehicle1;
  private Vehicle vehicle2;
  private WSEF wsef1;
  private WSEF wsef2;

  @BeforeEach
  void setUp() {
    vehicle1 = new Vehicle("car");
    vehicle2 = new Vehicle("truck");

    entityManager.persist(vehicle1);
    entityManager.persist(vehicle2);

    wsef1 = createWSEF(
      Condition.equal,
      "10.0",
      null,
      "0.0",
      Set.of(vehicle1)
    );

    wsef2 = createWSEF(
      Condition.inRange,
      "10.0",
      "20.0",
      "5.0",
      Set.of(vehicle1, vehicle2)
    );

    entityManager.persist(wsef1);
    entityManager.persist(wsef2);

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void findAll_shouldReturnAllWSEFsWithVehicles() {
    List<WSEF> result = wsefRepository.findAll();

    assertThat(result).hasSize(2);
    assertThat(result)
      .extracting(WSEF::getId)
      .containsExactlyInAnyOrder(wsef1.getId(), wsef2.getId());

    WSEF resultWSEF = result.stream()
      .filter(w -> w.getId().equals(wsef2.getId()))
      .findFirst()
      .orElseThrow();

    assertThat(resultWSEF.getVehicles())
      .extracting(Vehicle::getId)
      .containsExactlyInAnyOrder(
        vehicle1.getId(),
        vehicle2.getId()
      );
  }

  @Test
  void findById_shouldReturnWSEFWithVehicles() {
    Optional<WSEF> result = wsefRepository.findById(wsef1.getId());

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getVehicles())
      .extracting(Vehicle::getId)
      .containsExactly(vehicle1.getId());
  }

  @Test
  void findById_shouldReturnEmptyWhenWSEFDoesNotExist() {
    assertThat(wsefRepository.findById(-1L)).isEmpty();
  }

  @Test
  void findAllByVehicles_Id_shouldReturnMatchingWSEFs() {
    List<WSEF> result =
      wsefRepository.findAllByVehicles_Id(vehicle2.getId());

    assertThat(result)
      .extracting(WSEF::getId)
      .containsExactly(wsef2.getId());

    assertThat(result.get(0).getVehicles())
      .extracting(Vehicle::getId)
      .containsExactlyInAnyOrder(
        vehicle1.getId(),
        vehicle2.getId()
      );
  }

  @Test
  void findAllByVehicles_Id_shouldReturnEmptyWhenNoWSEFMatches() {
    assertThat(wsefRepository.findAllByVehicles_Id(-1L)).isEmpty();
  }

  private WSEF createWSEF(
    Condition condition,
    String windSpeed,
    String maxWindSpeed,
    String fee,
    Set<Vehicle> vehicles
  ) {
    return new WSEF(
      condition,
      new BigDecimal(windSpeed),
      maxWindSpeed == null ? null : new BigDecimal(maxWindSpeed),
      fee == null ? null : new BigDecimal(fee),
      vehicles
    );
  }
}

