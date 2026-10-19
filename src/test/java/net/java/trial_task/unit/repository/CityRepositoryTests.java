package net.java.trial_task.unit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import net.java.trial_task.AppConfig;
import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.repository.data.CityRepository;

@DataJpaTest
@Transactional
@Import(AppConfig.class)
class CityRepositoryTests {
  @Autowired
  private CityRepository cityRepository;

  @Test
  void save_assignsIdAndPersists() {
    City saved = cityRepository.saveAndFlush(new City("Tallinn"));

    assertThat(saved.getId()).isNotNull();
    assertThat(cityRepository.findById(saved.getId()))
      .get().extracting(City::getCityName).isEqualTo("Tallinn");
  }

  @Test
  void findAll_returnsAllSaved() {
    cityRepository.save(new City("Tartu"));
    cityRepository.save(new City("Pärnu"));

    assertThat(cityRepository.findAll()).extracting(City::getCityName)
      .contains("Tartu", "Pärnu");
  }

  @Test
  void delete_removesEntity() {
    City saved = cityRepository.saveAndFlush(new City("Tartu"));

    cityRepository.delete(saved);
    cityRepository.flush();

    assertThat(cityRepository.findById(saved.getId())).isEmpty();
  }

  @Test
  void save_nullName_violatesNotNullConstraint() {
    assertThatThrownBy(() -> cityRepository.saveAndFlush(new City(null)))
      .isInstanceOf(ConstraintViolationException.class);
  }
}