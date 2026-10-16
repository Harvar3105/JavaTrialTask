package net.java.trial_task.shared.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.shared.domain.entity.data.WeatherData;

public interface WeatherRepository extends JpaRepository<WeatherData, Long> {

  Optional<WeatherData> findFirstByWmoCodeOrderByObservationTimeDesc(Integer wmoCode);

  Optional<WeatherData> findFirstByWmoCodeAndObservationTimeLessThanEqualOrderByObservationTimeDesc(
    Integer wmoCode,
    Instant at
  );

  boolean existsByWmoCodeAndObservationTime(Integer wmoCode, Instant observationTime);

  Page<WeatherData> findByWmoCode(Integer wmoCode, Pageable pageable);
}
