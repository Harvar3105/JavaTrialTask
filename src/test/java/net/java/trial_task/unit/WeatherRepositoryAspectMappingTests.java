package net.java.trial_task.unit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;

import net.java.trial_task.shared.aspect.WeatherRepositoryAspect;
import net.java.trial_task.shared.domain.entity.WeatherData;
import net.java.trial_task.shared.exceptions.WeatherDataIntegrityViolationException;
import net.java.trial_task.shared.exceptions.WeatherDataRepositoryUnavailableException;
import net.java.trial_task.shared.repository.WeatherRepository;

class WeatherRepositoryAspectMappingTests {

  private WeatherRepository target;
  private WeatherRepository proxy;

  @BeforeEach
  void setUp() {
    target = mock(WeatherRepository.class);
    AspectJProxyFactory factory = new AspectJProxyFactory(target);
    factory.addAspect(new WeatherRepositoryAspect());
    proxy = factory.getProxy();
  }

  static Stream<Arguments> wrappedExceptions() {
    return Stream.of(
      Arguments.of(new DataIntegrityViolationException("x"), WeatherDataIntegrityViolationException.class),
      Arguments.of(new DuplicateKeyException("x"), WeatherDataIntegrityViolationException.class),
      Arguments.of(new DataAccessResourceFailureException("x"), WeatherDataRepositoryUnavailableException.class),
      Arguments.of(new CannotGetJdbcConnectionException("x"), WeatherDataRepositoryUnavailableException.class),
      Arguments.of(new CannotCreateTransactionException("x"), WeatherDataRepositoryUnavailableException.class)
    );
  }

  @ParameterizedTest
  @MethodSource("wrappedExceptions")
  @DisplayName("repository exception is mapped to the matching domain exception")
  void mapsException(RuntimeException thrown, Class<? extends RuntimeException> expected) {
    when(target.findFirstByWmoCodeOrderByObservationTimeDesc(26038)).thenThrow(thrown);

    assertThatThrownBy(() -> proxy.findFirstByWmoCodeOrderByObservationTimeDesc(26038))
      .isInstanceOf(expected);
  }

  @Test
  @DisplayName("unavailable: original exception preserved as cause, method name in message")
  void unavailable_keepsCauseAndMethodName() {
    var original = new DataAccessResourceFailureException("down");
    when(target.findFirstByWmoCodeOrderByObservationTimeDesc(26038)).thenThrow(original);

    assertThatThrownBy(() -> proxy.findFirstByWmoCodeOrderByObservationTimeDesc(26038))
      .isInstanceOf(WeatherDataRepositoryUnavailableException.class)
      .hasCause(original)
      .hasMessageContaining("findFirstByWmoCodeOrderByObservationTimeDesc");
  }

  @Test
  @DisplayName("non-persistence exception: propagated unchanged")
  void foreignException_isNotWrapped() {
    var foreign = new IllegalStateException("boom");
    when(target.findFirstByWmoCodeOrderByObservationTimeDesc(26038)).thenThrow(foreign);

    assertThatThrownBy(() -> proxy.findFirstByWmoCodeOrderByObservationTimeDesc(26038))
      .isSameAs(foreign);
  }

  @Test
  @DisplayName("successful call: result returned as is")
  void success_returnsTargetResult() {
    var data = new WeatherData("Pärnu", 41803, null, null, null, Instant.EPOCH);
    when(target.findFirstByWmoCodeOrderByObservationTimeDesc(41803)).thenReturn(Optional.of(data));

    assertThat(proxy.findFirstByWmoCodeOrderByObservationTimeDesc(41803)).containsSame(data);
  }
}
