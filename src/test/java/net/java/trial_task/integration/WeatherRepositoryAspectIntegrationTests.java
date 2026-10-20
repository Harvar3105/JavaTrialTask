package net.java.trial_task.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import net.java.trial_task.shared.aspect.WeatherRepositoryAspect;
import net.java.trial_task.shared.domain.entity.WeatherData;
import net.java.trial_task.shared.exceptions.WeatherDataIntegrityViolationException;
import net.java.trial_task.shared.repository.WeatherRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;

@DataJpaTest
@ImportAutoConfiguration(AopAutoConfiguration.class)
@Import(WeatherRepositoryAspect.class)
class WeatherRepositoryAspectIntegrationTests {
  private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

  @Autowired private WeatherRepository repository;

  @Test
  @DisplayName("aspect is applied: repository bean is an AOP proxy")
  void repositoryBean_isAopProxy() {
    assertThat(AopUtils.isAopProxy(repository)).isTrue();
  }

  @Test
  @DisplayName("successful call: result returned without wrapping")
  void successfulCall_returnsResult() {
    WeatherData saved = repository.saveAndFlush(
      new WeatherData("Tartu-Tõravere", 26242, new BigDecimal("-2.1"),
        new BigDecimal("4.7"), "Light snow shower", T0));

    assertThat(repository.findById(saved.getId())).isPresent();
  }

  @Test
  @DisplayName("invalid data: WeatherDataIntegrityViolationException, not raw DataAccessException")
  void invalidData_throwsIntegrityViolation() {
    WeatherData invalid = new WeatherData(
      "Pärnu", 41803, new BigDecimal("0.0"), new BigDecimal("1.0"), "Clear", null);

    assertThatThrownBy(() -> repository.saveAndFlush(invalid))
      .isInstanceOf(WeatherDataIntegrityViolationException.class);
  }
}
