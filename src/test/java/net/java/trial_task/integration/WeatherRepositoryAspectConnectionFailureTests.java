package net.java.trial_task.integration;

import static org.mockito.Mockito.doThrow;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import net.java.trial_task.shared.aspect.WeatherRepositoryAspect;
import net.java.trial_task.shared.domain.entity.WeatherData;
import net.java.trial_task.shared.exceptions.WeatherDataRepositoryUnavailableException;
import net.java.trial_task.shared.repository.WeatherRepository;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ImportAutoConfiguration(AopAutoConfiguration.class)
@Import(WeatherRepositoryAspect.class)
class WeatherRepositoryAspectConnectionFailureTests {
  @Autowired private WeatherRepository repository;
  @MockitoSpyBean private DataSource dataSource;

  @Test
  @DisplayName("connection failure: wrapped as WeatherDataRepositoryUnavailableException")
  void connectionFailure_isWrappedAsUnavailable() throws Exception {
    doThrow(new SQLException("connection refused")).when(dataSource).getConnection();

    assertThatThrownBy(() -> repository.findFirstByWmoCodeOrderByObservationTimeDesc(26038))
      .isInstanceOf(WeatherDataRepositoryUnavailableException.class)
      .hasCauseInstanceOf(Exception.class);
  }

  @Test
  @DisplayName("connection failure on write: wrapped as Unavailable, not as IntegrityViolation")
  void connectionFailure_onWrite_isUnavailable() throws Exception {
    doThrow(new SQLException("connection refused")).when(dataSource).getConnection();

    WeatherData data = new WeatherData(
      "Pärnu", 41803, new BigDecimal("1.0"), new BigDecimal("1.0"), "Clear",
      Instant.parse("2026-01-01T10:00:00Z"));

    assertThatThrownBy(() -> repository.saveAndFlush(data))
      .isInstanceOf(WeatherDataRepositoryUnavailableException.class);
  }
}
