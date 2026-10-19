package net.java.trial_task.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.ATEFNotFoundException;
import net.java.trial_task.application.exceptions.VehicleNotFoundException;
import net.java.trial_task.application.repository.ATEFRepository;
import net.java.trial_task.application.repository.VehicleRepository;
import net.java.trial_task.application.service.ATEFService;

@ExtendWith(MockitoExtension.class)
class ATEFServiceTests {
  @Mock private ATEFRepository atefRepository;
  @Mock private VehicleRepository vehicleRepository;
  @InjectMocks private ATEFService atefService;

  private Vehicle vehicle;
  private ATEF atef;

  @BeforeEach
  void setUp() {
    vehicle = new Vehicle("car");

    atef = new ATEF(
      Condition.inRange,
      new BigDecimal("10.0"),
      new BigDecimal("20.0"),
      BigDecimal.ZERO,
      Set.of(vehicle)
    );
  }

  @Test
  void getAll_shouldReturnAllATEFs() {
    List<ATEF> expected = List.of(atef);

    when(atefRepository.findAll()).thenReturn(expected);

    List<ATEF> result = atefService.getAll();

    assertThat(result).isSameAs(expected);
    verify(atefRepository).findAll();
  }

  @Test
  void getById_shouldReturnATEFWhenFound() {
    when(atefRepository.findById(1L))
      .thenReturn(Optional.of(atef));

    ATEF result = atefService.getById(1L);

    assertThat(result).isSameAs(atef);
  }

  @Test
  void getById_shouldThrowWhenNotFound() {
    when(atefRepository.findById(1L))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> atefService.getById(1L))
      .isInstanceOf(ATEFNotFoundException.class);

    verify(atefRepository).findById(1L);
  }

  @Test
  void create_shouldSaveATEFWithResolvedVehicles() {
    Set<Long> vehicleIds = Set.of(1L);

    when(vehicleRepository.findAllById(vehicleIds))
      .thenReturn(List.of(vehicle));

    when(atefRepository.save(any(ATEF.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    ATEF result = atefService.create(
      Condition.inRange,
      new BigDecimal("10.0"),
      new BigDecimal("20.0"),
      BigDecimal.ZERO,
      vehicleIds
    );

    assertThat(result.getCondition()).isEqualTo(Condition.inRange);
    assertThat(result.getTemperature())
        .isEqualByComparingTo("10.0");
    assertThat(result.getMaxTemperature())
        .isEqualByComparingTo("20.0");
    assertThat(result.getFee()).isEqualByComparingTo("0");
    assertThat(result.getVehicles()).containsExactly(vehicle);

    verify(vehicleRepository).findAllById(vehicleIds);
    verify(atefRepository).save(any(ATEF.class));
  }

  @Test
  void create_shouldAllowNullVehicleIds() {
    when(atefRepository.save(any(ATEF.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    ATEF result = atefService.create(
      Condition.equal,
      new BigDecimal("10.0"),
      null,
      BigDecimal.ZERO,
      null
    );

    assertThat(result.getVehicles()).isEmpty();

    verifyNoInteractions(vehicleRepository);
    verify(atefRepository).save(any(ATEF.class));
  }

  @Test
  void create_shouldAllowEmptyVehicleIds() {
    when(atefRepository.save(any(ATEF.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    ATEF result = atefService.create(
      Condition.equal,
      new BigDecimal("10.0"),
      null,
      BigDecimal.ZERO,
      Set.of()
    );

    assertThat(result.getVehicles()).isEmpty();

    verifyNoInteractions(vehicleRepository);
  }

  @Test
  void create_shouldThrowWhenSomeVehiclesDoNotExist() {
    Set<Long> requestedIds = Set.of(1L, 2L);

    when(vehicleRepository.findAllById(requestedIds))
      .thenReturn(List.of(vehicle));

    assertThatThrownBy(() -> atefService.create(
      Condition.equal,
      new BigDecimal("10.0"),
      null,
      BigDecimal.ZERO,
      requestedIds
    ))
      .isInstanceOf(VehicleNotFoundException.class)
      .hasMessageContaining("Vehicles not found");

    verify(atefRepository, never()).save(any(ATEF.class));
  }

  @Test
  void update_shouldUpdateExistingATEF() {
    when(atefRepository.findById(1L))
      .thenReturn(Optional.of(atef));

    ATEF result = atefService.update(
      1L,
      Condition.inRange,
      new BigDecimal("12.0"),
      new BigDecimal("25.0"),
      new BigDecimal("5.0")
    );

    assertThat(result).isSameAs(atef);
    assertThat(result.getTemperature())
      .isEqualByComparingTo("12.0");
    assertThat(result.getMaxTemperature())
      .isEqualByComparingTo("25.0");
    assertThat(result.getFee())
      .isEqualByComparingTo("5.0");

    verify(atefRepository).findById(1L);
    verify(atefRepository, never()).save(any(ATEF.class));
  }

  @Test
  void update_shouldThrowWhenATEFDoesNotExist() {
    when(atefRepository.findById(1L))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> atefService.update(
      1L,
      Condition.inRange,
      new BigDecimal("12.0"),
      new BigDecimal("25.0"),
      BigDecimal.ZERO
    ))
      .isInstanceOf(ATEFNotFoundException.class);

    verify(atefRepository, never()).save(any(ATEF.class));
  }

  @Test
  void delete_shouldDeleteExistingATEF() {
    when(atefRepository.findById(1L))
      .thenReturn(Optional.of(atef));

    atefService.delete(1L);

    verify(atefRepository).delete(atef);
  }

  @Test
  void delete_shouldThrowWhenATEFDoesNotExist() {
    when(atefRepository.findById(1L))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> atefService.delete(1L))
      .isInstanceOf(ATEFNotFoundException.class);

    verify(atefRepository, never()).delete(any(ATEF.class));
  }
}

