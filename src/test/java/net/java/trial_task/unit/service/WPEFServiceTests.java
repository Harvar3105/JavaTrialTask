package net.java.trial_task.unit.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.data.VehicleNotFoundException;
import net.java.trial_task.application.exceptions.condition.WPEFNotFoundException;
import net.java.trial_task.application.repository.data.VehicleRepository;
import net.java.trial_task.application.repository.condition.WPEFRepository;
import net.java.trial_task.application.service.condition.WPEFService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WPEFServiceTests {
  @Mock private WPEFRepository wpefRepository;
  @Mock private VehicleRepository vehicleRepository;
  @InjectMocks private WPEFService wpefService;

  private Vehicle vehicle;
  private WPEF wpef;

  @BeforeEach
  void setUp() {
    vehicle = new Vehicle("car");
    wpef = new WPEF(
      Condition.related,
      "rain",
      new BigDecimal("5.0"),
      Set.of(vehicle)
    );
  }

  @Test
  void getAll_shouldReturnAllWPEFs() {
    List<WPEF> expected = List.of(wpef);
    when(wpefRepository.findAll()).thenReturn(expected);

    List<WPEF> result = wpefService.getAll();

    assertThat(result).isSameAs(expected);
    verify(wpefRepository).findAll();
  }

  @Test
  void getById_shouldReturnWPEFWhenFound() {
    when(wpefRepository.findById(1L)).thenReturn(Optional.of(wpef));

    WPEF result = wpefService.getById(1L);

    assertThat(result).isSameAs(wpef);
    verify(wpefRepository).findById(1L);
  }

  @Test
  void getById_shouldThrowWhenNotFound() {
    when(wpefRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> wpefService.getById(99L))
      .isInstanceOf(WPEFNotFoundException.class);

    verify(wpefRepository).findById(99L);
  }

  @Test
  void create_shouldSaveWPEFWithResolvedVehicles() {
    Set<Long> vehicleIds = Set.of(1L);

    when(vehicleRepository.findAllById(vehicleIds))
      .thenReturn(List.of(vehicle));

    when(wpefRepository.save(any(WPEF.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    WPEF result = wpefService.create(
      Condition.related,
      "rain",
      new BigDecimal("5.0"),
      vehicleIds
    );

    assertThat(result.getCondition()).isEqualTo(Condition.related);
    assertThat(result.getPhenomenon()).isEqualTo("rain");
    assertThat(result.getFee()).isEqualByComparingTo("5.0");
    assertThat(result.getVehicles()).containsExactly(vehicle);

    verify(vehicleRepository).findAllById(vehicleIds);
    verify(wpefRepository).save(any(WPEF.class));
  }

  @Test
  void create_shouldAllowEmptyVehicleIds() {
    when(wpefRepository.save(any(WPEF.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    WPEF result = wpefService.create(
      Condition.related,
      "rain",
      BigDecimal.ZERO,
      Set.of()
    );

    assertThat(result.getVehicles()).isEmpty();
    verifyNoInteractions(vehicleRepository);
    verify(wpefRepository).save(any(WPEF.class));
  }

  @Test
  void create_shouldAllowNullVehicleIds() {
    when(wpefRepository.save(any(WPEF.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    WPEF result = wpefService.create(
      Condition.related,
      "rain",
      BigDecimal.ZERO,
      null
    );

    assertThat(result.getVehicles()).isEmpty();
    verifyNoInteractions(vehicleRepository);
  }

  @Test
  void create_shouldThrowWhenSomeVehiclesDoNotExist() {
    Set<Long> requestedIds = Set.of(1L, 2L);

    when(vehicleRepository.findAllById(requestedIds))
      .thenReturn(List.of(vehicle));

    assertThatThrownBy(() -> wpefService.create(
      Condition.related,
      "rain",
      BigDecimal.ZERO,
      requestedIds
    ))
      .isInstanceOf(VehicleNotFoundException.class)
      .hasMessageContaining("Vehicles not found");

    verify(wpefRepository, never()).save(any(WPEF.class));
  }

  @Test
  void update_shouldUpdateExistingWPEF() {
    when(wpefRepository.findById(1L)).thenReturn(Optional.of(wpef));

    WPEF result = wpefService.update(
      1L,
      Condition.related,
      "snow",
      new BigDecimal("7.0")
    );

    assertThat(result).isSameAs(wpef);
    assertThat(result.getCondition()).isEqualTo(Condition.related);
    assertThat(result.getPhenomenon()).isEqualTo("snow");
    assertThat(result.getFee()).isEqualByComparingTo("7.0");

    verify(wpefRepository).findById(1L);
    verify(wpefRepository, never()).save(any(WPEF.class));
  }

  @Test
  void update_shouldThrowWhenWPEFDoesNotExist() {
    when(wpefRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> wpefService.update(
      99L,
      Condition.related,
      "snow",
      BigDecimal.ZERO
    ))
      .isInstanceOf(WPEFNotFoundException.class);

    verify(wpefRepository, never()).save(any(WPEF.class));
  }

  @Test
  void delete_shouldDeleteExistingWPEF() {
    when(wpefRepository.findById(1L)).thenReturn(Optional.of(wpef));

    wpefService.delete(1L);

    verify(wpefRepository).delete(wpef);
  }

  @Test
  void delete_shouldThrowWhenWPEFDoesNotExist() {
    when(wpefRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> wpefService.delete(99L))
      .isInstanceOf(WPEFNotFoundException.class);

    verify(wpefRepository, never()).delete(any(WPEF.class));
  }
}

