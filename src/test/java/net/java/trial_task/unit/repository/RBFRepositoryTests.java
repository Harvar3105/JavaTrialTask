package net.java.trial_task.unit.repository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.AppConfig;
import net.java.trial_task.application.domain.entity.condition.RBF;
import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.repository.data.CityRepository;
import net.java.trial_task.application.repository.condition.RBFRepository;
import net.java.trial_task.application.repository.data.VehicleRepository;

@DataJpaTest
@Transactional
@Import(AppConfig.class)
class RBFRepositoryTests {
  @Autowired private RBFRepository repository;
  @Autowired private CityRepository cityRepository;
  @Autowired private VehicleRepository vehicleRepository;

  private Long cityId;
  private Long otherCityId;
  private Long vehicleId;
  private Long otherVehicleId;

  @BeforeEach
  void setUp() {
    cityId = cityRepository.saveAndFlush(new City("Tallinn")).getId();
    otherCityId = cityRepository.saveAndFlush(new City("Tartu")).getId();
    vehicleId = vehicleRepository.saveAndFlush(new Vehicle("Car")).getId();
    otherVehicleId = vehicleRepository.saveAndFlush(new Vehicle("Bike")).getId();
  }

  @Test
  void save_newEntity_databaseGeneratesIdAndTimestamps() {
    RBF saved = repository.saveAndFlush(new RBF(cityId, vehicleId, new BigDecimal("4.0")));

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
  }

  @Test
  void save_nullFee_isPersisted() {
    RBF saved = repository.saveAndFlush(new RBF(cityId, vehicleId, null));

    assertThat(repository.findById(saved.getId())).get()
      .extracting(RBF::getFee).isNull();
  }

  @Test
  void save_fee_isStoredWithScaleOne() {
    RBF saved = repository.saveAndFlush(new RBF(cityId, vehicleId, new BigDecimal("3.5")));

    BigDecimal stored = repository.findById(saved.getId()).orElseThrow().getFee();
    assertThat(stored).isEqualByComparingTo("3.5");
  }

  @Test
  void save_feeExceedsPrecision_throws() {
    RBF entity = new RBF(cityId, vehicleId, new BigDecimal("12345.6"));

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
      .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void save_nullCityId_throws() {
    RBF entity = new RBF(null, vehicleId, BigDecimal.ONE);

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
      .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void save_nullVehicleTypeId_throws() {
    RBF entity = new RBF(cityId, null, BigDecimal.ONE);

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
      .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void save_unknownCityId_throws() {
    RBF entity = new RBF(Long.MAX_VALUE, vehicleId, BigDecimal.ONE);

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
      .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void save_unknownVehicleTypeId_throws() {
    RBF entity = new RBF(cityId, Long.MAX_VALUE, BigDecimal.ONE);

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
      .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void findByCityIdAndVehicleTypeId_existingPair_returnsEntity() {
    RBF saved = repository.saveAndFlush(new RBF(cityId, vehicleId, BigDecimal.ONE));
    repository.saveAndFlush(new RBF(cityId, otherVehicleId, BigDecimal.TEN));

    assertThat(repository.findByCityIdAndVehicleTypeId(cityId, vehicleId))
      .get().extracting(RBF::getId).isEqualTo(saved.getId());
  }

  @Test
  void findByCityIdAndVehicleTypeId_absentPair_returnsEmpty() {
    repository.saveAndFlush(new RBF(cityId, vehicleId, BigDecimal.ONE));

    assertThat(repository.findByCityIdAndVehicleTypeId(otherCityId, vehicleId)).isEmpty();
  }

  @Test
  void existsByCityIdAndVehicleTypeId_reflectsPresence() {
    repository.saveAndFlush(new RBF(cityId, vehicleId, BigDecimal.ONE));

    assertThat(repository.existsByCityIdAndVehicleTypeId(cityId, vehicleId)).isTrue();
    assertThat(repository.existsByCityIdAndVehicleTypeId(cityId, otherVehicleId)).isFalse();
  }

  @Test
  void existsByCityIdAndVehicleTypeIdAndIdNot_ownRecord_returnsFalse() {
    RBF saved = repository.saveAndFlush(new RBF(cityId, vehicleId, BigDecimal.ONE));

    assertThat(repository.existsByCityIdAndVehicleTypeIdAndIdNot(cityId, vehicleId, saved.getId()))
      .isFalse();
  }

  @Test
  void existsByCityIdAndVehicleTypeIdAndIdNot_otherRecord_returnsTrue() {
    RBF saved = repository.saveAndFlush(new RBF(cityId, vehicleId, BigDecimal.ONE));

    assertThat(repository.existsByCityIdAndVehicleTypeIdAndIdNot(
      cityId, vehicleId, saved.getId() + 1)).isTrue();
  }

  @Test
  void delete_existingEntity_removesIt() {
    RBF saved = repository.saveAndFlush(new RBF(cityId, vehicleId, BigDecimal.ONE));

    repository.delete(saved);
    repository.flush();

    assertThat(repository.findById(saved.getId())).isEmpty();
  }
}