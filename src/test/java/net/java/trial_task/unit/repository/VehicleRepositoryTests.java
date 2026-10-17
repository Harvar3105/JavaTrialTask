package net.java.trial_task.unit.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolationException;
import net.java.trial_task.AppConfig;
import net.java.trial_task.application.repository.VehicleRepository;
import net.java.trial_task.shared.domain.entity.data.Vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Transactional
@Import(AppConfig.class)
class VehicleRepositoryTests {
  @Autowired
  private VehicleRepository repository;

  @Test
  void save_newEntity_databaseGeneratesIdAndTimestamps() {
    Vehicle saved = repository.saveAndFlush(new Vehicle("BIKE"));

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
    assertThat(saved.getVehicleType()).isEqualTo("BIKE");
  }

  @Test
  void save_duplicateVehicleType_throwsDataIntegrityViolation() {
    repository.saveAndFlush(new Vehicle("CAR"));

    assertThatThrownBy(() -> repository.saveAndFlush(new Vehicle("CAR")))
      .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void save_nullVehicleType_throwsDataIntegrityViolation() {
    assertThatThrownBy(() -> repository.saveAndFlush(new Vehicle(null)))
      .isInstanceOf(ConstraintViolationException.class);
  }

  @Test
  void update_changesVehicleType_keepsCreatedAt() {
    Vehicle saved = repository.saveAndFlush(new Vehicle("SCOOTER"));
    var createdAt = saved.getCreatedAt();

    saved.setVehicleType("MOPED");
    Vehicle updated = repository.saveAndFlush(saved);

    assertThat(updated.getVehicleType()).isEqualTo("MOPED");
    assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
    assertThat(updated.getUpdatedAt()).isNotNull();
  }

  @Test
  void existsByVehicleType_existingType_returnsTrue() {
    repository.saveAndFlush(new Vehicle("BIKE"));

    assertThat(repository.existsByVehicleType("BIKE")).isTrue();
  }

  @Test
  void existsByVehicleType_missingType_returnsFalse() {
    assertThat(repository.existsByVehicleType("BIKE")).isFalse();
  }

  @Test
  void existsByVehicleTypeAndIdNot_sameRecord_returnsFalse() {
    Vehicle saved = repository.saveAndFlush(new Vehicle("BIKE"));

    assertThat(repository.existsByVehicleTypeAndIdNot("BIKE", saved.getId())).isFalse();
  }

  @Test
  void existsByVehicleTypeAndIdNot_otherRecord_returnsTrue() {
    Vehicle bike = repository.saveAndFlush(new Vehicle("BIKE"));
    Vehicle car = repository.saveAndFlush(new Vehicle("CAR"));

    assertThat(repository.existsByVehicleTypeAndIdNot("BIKE", car.getId())).isTrue();
    assertThat(bike.getId()).isNotEqualTo(car.getId());
  }

  @Test
  void delete_existingEntity_removesIt() {
    Vehicle saved = repository.saveAndFlush(new Vehicle("CAR"));

    repository.delete(saved);
    repository.flush();

    assertThat(repository.findById(saved.getId())).isEmpty();
  }
}