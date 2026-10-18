package net.java.trial_task.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.exceptions.CityNotFoundException;
import net.java.trial_task.application.repository.CityRepository;
import net.java.trial_task.application.service.CityService;

@ExtendWith(MockitoExtension.class)
class CityServiceTests {
  @Mock
  private CityRepository cityRepository;

  @InjectMocks
  private CityService cityService;

  @Test
  void create_savesAndReturnsCity() {
    when(cityRepository.saveAndFlush(any(City.class))).thenAnswer(inv -> inv.getArgument(0));

    City result = cityService.create("Tallinn");

    assertThat(result.getCityName()).isEqualTo("Tallinn");
    verify(cityRepository).saveAndFlush(any(City.class));
  }

  @Test
  void findAll_returnsAllCities() {
    when(cityRepository.findAll()).thenReturn(List.of(new City("Tallinn"), new City("Tartu")));

    assertThat(cityService.findAll()).extracting(City::getCityName)
      .containsExactly("Tallinn", "Tartu");
  }

  @Test
  void findById_existing_returnsCity() {
    when(cityRepository.findById(1L)).thenReturn(Optional.of(new City("Pärnu")));

    assertThat(cityService.findById(1L).getCityName()).isEqualTo("Pärnu");
  }

  @Test
  void findById_missing_throwsCityNotFoundException() {
    when(cityRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cityService.findById(99L))
      .isInstanceOf(CityNotFoundException.class)
      .hasMessageContaining("99");
  }

  @Test
  void update_existing_changesName() {
    City existing = new City("Old");
    when(cityRepository.findById(1L)).thenReturn(Optional.of(existing));

    City result = cityService.update(1L, "New");

    assertThat(result.getCityName()).isEqualTo("New");
  }

  @Test
  void update_missing_throwsCityNotFoundException() {
    when(cityRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cityService.update(99L, "New"))
      .isInstanceOf(CityNotFoundException.class);
  }

  @Test
  void delete_existing_deletesEntity() {
    City existing = new City("Tartu");
    when(cityRepository.findById(1L)).thenReturn(Optional.of(existing));

    cityService.delete(1L);

    verify(cityRepository).delete(existing);
  }

  @Test
  void delete_missing_throwsAndDoesNotDelete() {
    when(cityRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cityService.delete(99L))
      .isInstanceOf(CityNotFoundException.class);
    verify(cityRepository, never()).delete(any(City.class));
  }
}
