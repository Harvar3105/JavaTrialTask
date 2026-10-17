package net.java.trial_task.unit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.shared.domain.entity.WeatherData;
import net.java.trial_task.shared.repository.WeatherRepository;

@DataJpaTest
@Transactional
public class WeatherDataRepositoryTests {

  private static final int TALLINN = 26038;
  private static final int TARTU = 26242;
  private static final int PARNU = 41803;

  private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

  @Autowired
  private WeatherRepository repository;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
    repository.flush();
  }

  private WeatherData data(int wmo, String station, Instant time, String temp) {
    return new WeatherData(
      station,
      wmo,
      new BigDecimal(temp),
      new BigDecimal("4.7"),
      "Light snow shower",
      time
    );
  }

  @Test
  @DisplayName("save: persists entity, generates id, preserves values")
  void save_persistsAllFields() {
    WeatherData saved = repository.saveAndFlush(data(TARTU, "Tartu-Tõravere", T0, "-2.1"));

    WeatherData found = repository.findById(saved.getId()).orElseThrow();

    assertThat(found.getStation()).isEqualTo("Tartu-Tõravere");
    assertThat(found.getWmoCode()).isEqualTo(TARTU);
    assertThat(found.getAirTemperature()).isEqualByComparingTo("-2.1");
    assertThat(found.getWindSpeed()).isEqualByComparingTo("4.7");
    assertThat(found.getPhenomenon()).isEqualTo("Light snow shower");
    assertThat(found.getObservationTime()).isEqualTo(T0);
  }

  @Test
  @DisplayName("save: records of the same station are appended, not overwritten")
  void save_keepsHistory() {
    repository.save(data(TALLINN, "Tallinn-Harku", T0, "1.0"));
    repository.save(data(TALLINN, "Tallinn-Harku", T0.plus(1, ChronoUnit.HOURS), "2.0"));
    repository.flush();

    assertThat(repository.count()).isEqualTo(2);
  }

  @Test
  @DisplayName("save: null observationTime violates NOT NULL constraint")
  void save_nullObservationTime_throws() {
    WeatherData invalid = new WeatherData(
      "Pärnu", PARNU, new BigDecimal("0.0"), new BigDecimal("1.0"), "Clear", null
    );

    assertThatThrownBy(() -> repository.saveAndFlush(invalid))
      .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("findFirstByWmoCodeOrderByObservationTimeDesc: returns the latest record")
  void findLatest_returnsNewest() {
    repository.save(data(TALLINN, "Tallinn-Harku", T0, "1.0"));
    repository.save(data(TALLINN, "Tallinn-Harku", T0.plus(2, ChronoUnit.HOURS), "3.0"));
    repository.save(data(TALLINN, "Tallinn-Harku", T0.plus(1, ChronoUnit.HOURS), "2.0"));
    repository.flush();

    WeatherData latest = repository.findFirstByWmoCodeOrderByObservationTimeDesc(TALLINN).orElseThrow();

    assertThat(latest.getObservationTime()).isEqualTo(T0.plus(2, ChronoUnit.HOURS));
    assertThat(latest.getAirTemperature()).isEqualByComparingTo("3.0");
  }

  @Test
  @DisplayName("findFirstByWmoCodeOrderByObservationTimeDesc: ignores other stations")
  void findLatest_filtersByWmoCode() {
    repository.save(data(TALLINN, "Tallinn-Harku", T0.plus(5, ChronoUnit.HOURS), "9.0"));
    repository.save(data(TARTU, "Tartu-Tõravere", T0, "-2.1"));
    repository.flush();

    WeatherData latest = repository.findFirstByWmoCodeOrderByObservationTimeDesc(TARTU).orElseThrow();

    assertThat(latest.getStation()).isEqualTo("Tartu-Tõravere");
    assertThat(latest.getObservationTime()).isEqualTo(T0);
  }

  @Test
  @DisplayName("findFirstByWmoCodeOrderByObservationTimeDesc: empty when no data for station")
  void findLatest_noData_returnsEmpty() {
    repository.save(data(TALLINN, "Tallinn-Harku", T0, "1.0"));
    repository.flush();

    assertThat(repository.findFirstByWmoCodeOrderByObservationTimeDesc(PARNU)).isEmpty();
  }

  @Test
  @DisplayName("findFirst...LessThanEqual: returns the latest record not after the given time")
  void findAt_returnsLatestNotAfter() {
    repository.save(data(PARNU, "Pärnu", T0, "1.0"));
    repository.save(data(PARNU, "Pärnu", T0.plus(1, ChronoUnit.HOURS), "2.0"));
    repository.save(data(PARNU, "Pärnu", T0.plus(2, ChronoUnit.HOURS), "3.0"));
    repository.flush();

    WeatherData found = repository
      .findFirstByWmoCodeAndObservationTimeLessThanEqualOrderByObservationTimeDesc(
        PARNU, T0.plus(90, ChronoUnit.MINUTES))
      .orElseThrow();

    assertThat(found.getObservationTime()).isEqualTo(T0.plus(1, ChronoUnit.HOURS));
    assertThat(found.getAirTemperature()).isEqualByComparingTo("2.0");
  }

  @Test
  @DisplayName("findFirst...LessThanEqual: boundary is inclusive")
  void findAt_exactTimestamp_isIncluded() {
    Instant exact = T0.plus(1, ChronoUnit.HOURS);
    repository.save(data(PARNU, "Pärnu", T0, "1.0"));
    repository.save(data(PARNU, "Pärnu", exact, "2.0"));
    repository.flush();

    WeatherData found = repository
      .findFirstByWmoCodeAndObservationTimeLessThanEqualOrderByObservationTimeDesc(PARNU, exact)
      .orElseThrow();

    assertThat(found.getObservationTime()).isEqualTo(exact);
  }

  @Test
  @DisplayName("findFirst...LessThanEqual: empty when all records are later than the given time")
  void findAt_beforeEarliest_returnsEmpty() {
    repository.save(data(PARNU, "Pärnu", T0, "1.0"));
    repository.flush();

    assertThat(
      repository.findFirstByWmoCodeAndObservationTimeLessThanEqualOrderByObservationTimeDesc(
        PARNU, T0.minusSeconds(1))
    ).isEmpty();
  }

  @Test
  @DisplayName("findFirst...LessThanEqual: ignores records of other stations")
  void findAt_filtersByWmoCode() {
    repository.save(data(TALLINN, "Tallinn-Harku", T0, "1.0"));
    repository.flush();

    assertThat(
      repository.findFirstByWmoCodeAndObservationTimeLessThanEqualOrderByObservationTimeDesc(
        TARTU, T0.plus(1, ChronoUnit.DAYS))
    ).isEmpty();
  }

  @Test
  @DisplayName("existsByWmoCodeAndObservationTime: true for exact match")
  void exists_exactMatch_returnsTrue() {
    repository.save(data(TALLINN, "Tallinn-Harku", T0, "1.0"));
    repository.flush();

    assertThat(repository.existsByWmoCodeAndObservationTime(TALLINN, T0)).isTrue();
  }

  @Test
  @DisplayName("existsByWmoCodeAndObservationTime: false for different time")
  void exists_differentTime_returnsFalse() {
    repository.save(data(TALLINN, "Tallinn-Harku", T0, "1.0"));
    repository.flush();

    assertThat(
      repository.existsByWmoCodeAndObservationTime(TALLINN, T0.plusSeconds(1))
    ).isFalse();
  }

  @Test
  @DisplayName("existsByWmoCodeAndObservationTime: false for different station")
  void exists_differentWmoCode_returnsFalse() {
    repository.save(data(TALLINN, "Tallinn-Harku", T0, "1.0"));
    repository.flush();

    assertThat(repository.existsByWmoCodeAndObservationTime(TARTU, T0)).isFalse();
  }

  @Test
  @DisplayName("findByWmoCode: returns only the requested station, paginated")
  void findByWmoCode_paginatesAndFilters() {
    for (int i = 0; i < 5; i++) {
      repository.save(data(TALLINN, "Tallinn-Harku", T0.plus(i, ChronoUnit.HOURS), "1.0"));
    }
    repository.save(data(TARTU, "Tartu-Tõravere", T0, "-2.1"));
    repository.flush();

    Page<WeatherData> page = repository.findByWmoCode(
      TALLINN,
      PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "observationTime"))
    );

    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(3);
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getContent())
      .extracting(WeatherData::getWmoCode)
      .containsOnly(TALLINN);
    assertThat(page.getContent().get(0).getObservationTime())
      .isEqualTo(T0.plus(4, ChronoUnit.HOURS));
  }

  @Test
  @DisplayName("findByWmoCode: empty page when no data for station")
  void findByWmoCode_noData_returnsEmptyPage() {
    Page<WeatherData> page = repository.findByWmoCode(PARNU, PageRequest.of(0, 10));

    assertThat(page.isEmpty()).isTrue();
    assertThat(page.getTotalElements()).isZero();
  }
}