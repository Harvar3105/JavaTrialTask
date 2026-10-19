package net.java.trial_task.unit.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.data.VehicleNotFoundException;
import net.java.trial_task.application.exceptions.condition.WSEFNotFoundException;
import net.java.trial_task.application.repository.data.VehicleRepository;
import net.java.trial_task.application.repository.condition.WSEFRepository;
import net.java.trial_task.application.service.condition.WSEFService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WSEFServiceTests {
  @Mock private WSEFRepository wsefRepository;
  @Mock private VehicleRepository vehicleRepository;
  @InjectMocks private WSEFService wsefService;

  private Vehicle vehicle;
  private WSEF wsef;

  @BeforeEach
  void setUp() {
    vehicle = new Vehicle("car");

    wsef = new WSEF(
      Condition.inRange,
      new BigDecimal("10.0"),
      new BigDecimal("20.0"),
      new BigDecimal("5.0"),
      Set.of(vehicle)
    );
  }

  @Test
  void getAll_shouldReturnAllWSEFs() {
    List<WSEF> expected = List.of(wsef);
    when(wsefRepository.findAll()).thenReturn(expected);

    List<WSEF> result = wsefService.getAll();

    assertThat(result).isSameAs(expected);
    verify(wsefRepository).findAll();
  }

  @Test
  void getById_shouldReturnWSEFWhenFound() {
    when(wsefRepository.findById(1L)).thenReturn(Optional.of(wsef));

    WSEF result = wsefService.getById(1L);

    assertThat(result).isSameAs(wsef);
    verify(wsefRepository).findById(1L);
  }

  @Test
  void getById_shouldThrowWhenNotFound() {
    when(wsefRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> wsefService.getById(99L))
      .isInstanceOf(WSEFNotFoundException.class);

    verify(wsefRepository).findById(99L);
  }

  @Test
  void create_shouldSaveWSEFWithResolvedVehicles() {
    Set<Long> vehicleIds = Set.of(1L);

    when(vehicleRepository.findAllById(vehicleIds))
      .thenReturn(List.of(vehicle));

    when(wsefRepository.save(any(WSEF.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    WSEF result = wsefService.create(
      Condition.inRange,
      new BigDecimal("10.0"),
      new BigDecimal("20.0"),
      new BigDecimal("5.0"),
      vehicleIds
    );

    assertThat(result.getCondition()).isEqualTo(Condition.inRange);
    assertThat(result.getWindSpeed()).isEqualByComparingTo("10.0");
    assertThat(result.getMaxWindSpeed()).isEqualByComparingTo("20.0");
    assertThat(result.getFee()).isEqualByComparingTo("5.0");
    assertThat(result.getVehicles()).containsExactly(vehicle);

    verify(vehicleRepository).findAllById(vehicleIds);
    verify(wsefRepository).save(any(WSEF.class));
  }

  @Test
  void create_shouldAllowNullVehicleIds() {
    when(wsefRepository.save(any(WSEF.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    WSEF result = wsefService.create(
      Condition.equal,
      new BigDecimal("10.0"),
      null,
      BigDecimal.ZERO,
      null
    );

    assertThat(result.getVehicles()).isEmpty();
    verifyNoInteractions(vehicleRepository);
    verify(wsefRepository).save(any(WSEF.class));
  }

  @Test
  void create_shouldAllowEmptyVehicleIds() {
    when(wsefRepository.save(any(WSEF.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    WSEF result = wsefService.create(
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

    assertThatThrownBy(() -> wsefService.create(
      Condition.equal,
      new BigDecimal("10.0"),
      null,
      BigDecimal.ZERO,
      requestedIds
    ))
      .isInstanceOf(VehicleNotFoundException.class)
      .hasMessageContaining("Vehicles not found");

    verify(wsefRepository, never()).save(any(WSEF.class));
  }

  @Test
  void update_shouldUpdateExistingWSEF() {
    when(wsefRepository.findById(1L)).thenReturn(Optional.of(wsef));

    WSEF result = wsefService.update(
      1L,
      Condition.inRange,
      new BigDecimal("12.0"),
      new BigDecimal("25.0"),
      new BigDecimal("7.0")
    );

    assertThat(result).isSameAs(wsef);
    assertThat(result.getWindSpeed()).isEqualByComparingTo("12.0");
    assertThat(result.getMaxWindSpeed()).isEqualByComparingTo("25.0");
    assertThat(result.getFee()).isEqualByComparingTo("7.0");

    verify(wsefRepository).findById(1L);
    verify(wsefRepository, never()).save(any(WSEF.class));
  }

  @Test
  void update_shouldThrowWhenWSEFDoesNotExist() {
    when(wsefRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> wsefService.update(
      99L,
      Condition.inRange,
      new BigDecimal("12.0"),
      new BigDecimal("25.0"),
      BigDecimal.ZERO
    ))
      .isInstanceOf(WSEFNotFoundException.class);

    verify(wsefRepository, never()).save(any(WSEF.class));
  }

  @Test
  void delete_shouldDeleteExistingWSEF() {
    when(wsefRepository.findById(1L)).thenReturn(Optional.of(wsef));

    wsefService.delete(1L);

    verify(wsefRepository).delete(wsef);
  }

  @Test
  void delete_shouldThrowWhenWSEFDoesNotExist() {
    when(wsefRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> wsefService.delete(99L))
      .isInstanceOf(WSEFNotFoundException.class);

    verify(wsefRepository, never()).delete(any(WSEF.class));
  }
}
