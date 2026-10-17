package net.java.trial_task.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import net.java.trial_task.application.exceptions.VehicleAlreadyExistsException;
import net.java.trial_task.application.exceptions.VehicleNotFoundException;
import net.java.trial_task.application.repository.VehicleRepository;
import net.java.trial_task.application.service.VehicleService;
import net.java.trial_task.shared.domain.entity.data.Vehicle;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTests {
  @Mock
  private VehicleRepository repository;

  @InjectMocks
  private VehicleService service;

  private static Vehicle vehicle(Long id, String type) {
    Vehicle v = new Vehicle(type);
    ReflectionTestUtils.setField(v, "id", id);
    return v;
  }

  @Test
  void findAll_returnsAllVehicles() {
    List<Vehicle> vehicles = List.of(vehicle(1L, "BIKE"), vehicle(2L, "CAR"));
    when(repository.findAll()).thenReturn(vehicles);

    assertThat(service.findAll()).containsExactlyElementsOf(vehicles);
  }

  @Test
  void findById_existing_returnsVehicle() {
    Vehicle v = vehicle(1L, "BIKE");
    when(repository.findById(1L)).thenReturn(Optional.of(v));

    assertThat(service.findById(1L)).isSameAs(v);
  }

  @Test
  void findById_missing_throwsNotFound() {
    when(repository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(1L))
      .isInstanceOf(VehicleNotFoundException.class)
      .hasMessageContaining("1");
  }

  @Test
  void create_newType_savesAndReturnsVehicle() {
    when(repository.existsByVehicleType("BIKE")).thenReturn(false);
    when(repository.saveAndFlush(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

    Vehicle created = service.create("BIKE");

    assertThat(created.getVehicleType()).isEqualTo("BIKE");
    verify(repository).saveAndFlush(any(Vehicle.class));
  }

  @Test
  void create_duplicateType_throwsAlreadyExistsAndDoesNotSave() {
    when(repository.existsByVehicleType("BIKE")).thenReturn(true);

    assertThatThrownBy(() -> service.create("BIKE"))
      .isInstanceOf(VehicleAlreadyExistsException.class);
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void update_existingWithFreeType_changesTypeAndSaves() {
    Vehicle v = vehicle(1L, "BIKE");
    when(repository.findById(1L)).thenReturn(Optional.of(v));
    when(repository.existsByVehicleTypeAndIdNot("MOPED", 1L)).thenReturn(false);
    when(repository.saveAndFlush(v)).thenReturn(v);

    Vehicle updated = service.update(1L, "MOPED");

    assertThat(updated.getVehicleType()).isEqualTo("MOPED");
    verify(repository).saveAndFlush(v);
  }

  @Test
  void update_missing_throwsNotFoundAndDoesNotSave() {
    when(repository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(1L, "MOPED"))
      .isInstanceOf(VehicleNotFoundException.class);
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void update_typeUsedByAnotherVehicle_throwsAlreadyExistsAndDoesNotSave() {
    Vehicle v = vehicle(1L, "BIKE");
    when(repository.findById(1L)).thenReturn(Optional.of(v));
    when(repository.existsByVehicleTypeAndIdNot("CAR", 1L)).thenReturn(true);

    assertThatThrownBy(() -> service.update(1L, "CAR"))
      .isInstanceOf(VehicleAlreadyExistsException.class);
    verify(repository, never()).saveAndFlush(any());
  }

  @Test
  void delete_existing_deletesEntity() {
    Vehicle v = vehicle(1L, "BIKE");
    when(repository.findById(1L)).thenReturn(Optional.of(v));

    service.delete(1L);

    verify(repository).delete(v);
  }

  @Test
  void delete_missing_throwsNotFoundAndDoesNotDelete() {
    when(repository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(1L))
      .isInstanceOf(VehicleNotFoundException.class);
    verify(repository, never()).delete(any());
  }
}