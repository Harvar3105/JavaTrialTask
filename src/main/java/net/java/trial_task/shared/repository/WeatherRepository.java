package net.java.trial_task.shared.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.shared.domain.entity.WeatherData;

public interface WeatherRepository extends JpaRepository<WeatherData, Long> {
    /**
   * Finds the most recent stored observation among stations whose name contains the given
   * text, ignoring case.
   *
   * <p>Records are ordered by {@code observationTime} descending and the first one is
   * returned. If several stations match, the observation with the latest time among all of
   * them is returned; the choice between equal times is not defined.
   *
   * @param station text to look for in the station name, must not be {@code null}
   * @return the latest matching observation, or {@link Optional#empty()} if none exists
   */
  Optional<WeatherData> findFirstByStationContainingIgnoreCaseOrderByObservationTimeDesc(String station);

   /**
   * Finds the most recent observation made at or before the given moment among stations
   * whose name contains the given text, ignoring case.
   *
   * <p>The upper bound is inclusive; no lower bound applies. Selection among several
   * matching stations and equal times follows
   * {@link #findFirstByStationContainingIgnoreCaseOrderByObservationTimeDesc(String)}.
   *
   * @param station text to look for in the station name, must not be {@code null}
   * @param at the moment for which the observation is requested, must not be {@code null}
   * @return the latest matching observation not later than {@code at}, or
   *     {@link Optional#empty()} if none exists
   */
  Optional<WeatherData> findFirstByStationContainingIgnoreCaseAndObservationTimeLessThanEqualOrderByObservationTimeDesc(
    String station,
    Instant at
  );

  Optional<WeatherData> findFirstByWmoCodeOrderByObservationTimeDesc(Integer wmoCode);

  Optional<WeatherData> findFirstByWmoCodeAndObservationTimeLessThanEqualOrderByObservationTimeDesc(
    Integer wmoCode,
    Instant at
  );

  boolean existsByWmoCodeAndObservationTime(Integer wmoCode, Instant observationTime);

  Page<WeatherData> findByWmoCode(Integer wmoCode, Pageable pageable);
}
