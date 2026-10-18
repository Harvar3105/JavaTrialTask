package net.java.trial_task.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.java.trial_task.application.domain.entity.condition.RBF;
import net.java.trial_task.application.exceptions.RBFNotFoundException;
import net.java.trial_task.application.repository.RBFRepository;
import net.java.trial_task.application.service.RBFService;

@ExtendWith(MockitoExtension.class)
class RBFServiceTests {
  @Mock
  private RBFRepository repository;

  @InjectMocks
  private RBFService service;

  @Test
  void findAll_returnsRepositoryContent() {
    List<RBF> all = List.of(new RBF(1L, 2L, BigDecimal.ONE));
    when(repository.findAll()).thenReturn(all);

    assertThat(service.findAll()).isEqualTo(all);
  }

  @Test
  void findById_existing_returnsEntity() {
    RBF entity = new RBF(1L, 2L, BigDecimal.ONE);
    when(repository.findById(5L)).thenReturn(Optional.of(entity));

    assertThat(service.findById(5L)).isSameAs(entity);
  }

  @Test
  void findById_absent_throwsNotFound() {
    when(repository.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(5L)).isInstanceOf(RBFNotFoundException.class);
  }

  @Test
  void create_savesEntityWithGivenValues() {
    when(repository.saveAndFlush(any(RBF.class))).thenAnswer(inv -> inv.getArgument(0));

    RBF result = service.create(1L, 2L, new BigDecimal("3.5"));

    ArgumentCaptor<RBF> captor = ArgumentCaptor.forClass(RBF.class);
    verify(repository).saveAndFlush(captor.capture());
    RBF saved = captor.getValue();
    assertThat(saved.getCityId()).isEqualTo(1L);
    assertThat(saved.getVehicleTypeId()).isEqualTo(2L);
    assertThat(saved.getFee()).isEqualByComparingTo("3.5");
    assertThat(result).isSameAs(saved);
  }

  @Test
  void create_nullFee_savesEntityWithNullFee() {
    when(repository.saveAndFlush(any(RBF.class))).thenAnswer(inv -> inv.getArgument(0));

    RBF result = service.create(1L, 2L, null);

    assertThat(result.getFee()).isNull();
  }

  @Test
  void updateFee_existing_changesOnlyFee() {
    RBF entity = new RBF(1L, 2L, BigDecimal.ONE);
    when(repository.findById(5L)).thenReturn(Optional.of(entity));

    RBF result = service.updateFee(5L, new BigDecimal("9.9"));

    assertThat(result.getFee()).isEqualByComparingTo("9.9");
    assertThat(result.getCityId()).isEqualTo(1L);
    assertThat(result.getVehicleTypeId()).isEqualTo(2L);
  }

  @Test
  void updateFee_nullFee_setsNull() {
    RBF entity = new RBF(1L, 2L, BigDecimal.ONE);
    when(repository.findById(5L)).thenReturn(Optional.of(entity));

    assertThat(service.updateFee(5L, null).getFee()).isNull();
  }

  @Test
  void updateFee_absent_throwsNotFound() {
    when(repository.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateFee(5L, BigDecimal.ONE))
        .isInstanceOf(RBFNotFoundException.class);
  }

  @Test
  void delete_existing_deletesEntity() {
    RBF entity = new RBF(1L, 2L, BigDecimal.ONE);
    when(repository.findById(5L)).thenReturn(Optional.of(entity));

    service.delete(5L);

    verify(repository).delete(entity);
  }

  @Test
  void delete_absent_throwsNotFoundAndDeletesNothing() {
    when(repository.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(RBFNotFoundException.class);
    verify(repository, never()).delete(any());
  }
}
