package net.java.trial_task.shared.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.shared.domain.entity.WeatherData;

/**
 * Data access for {@link WeatherData} observations.
 *
 * <p>Observations are append-only history: one record per station per observation time.
 * Stations are identified by their WMO code. Methods returning a single record rely on
 * {@code observationTime} ordering to choose between several stored observations.
 */
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

  /**
   * Finds the most recent stored observation of a station.
   *
   * <p>Records are ordered by {@code observationTime} descending and the first one is returned.
   * If several records share the latest {@code observationTime}, the choice between them is
   * not defined.
   *
   * @param wmoCode WMO code of the station, must not be {@code null}
   * @return the latest observation, or {@link Optional#empty()} if the station has no stored
   *     observations
   */
  Optional<WeatherData> findFirstByWmoCodeOrderByObservationTimeDesc(Integer wmoCode);

  /**
   * Finds the observation of a station that was valid at the given moment, i.e. the most
   * recent observation made at or before that moment.
   *
   * <p>The upper bound is inclusive: an observation with {@code observationTime} equal to
   * {@code at} is a match. Observations made after {@code at} are ignored. No lower bound
   * applies, so the result may be arbitrarily old. If several records share the selected
   * {@code observationTime}, the choice between them is not defined.
   *
   * @param wmoCode WMO code of the station, must not be {@code null}
   * @param at the moment for which the observation is requested, must not be {@code null}
   * @return the latest observation not later than {@code at}, or {@link Optional#empty()} if
   *     the station has no observation at or before that moment
   */
  Optional<WeatherData> findFirstByWmoCodeAndObservationTimeLessThanEqualOrderByObservationTimeDesc(
    Integer wmoCode,
    Instant at
  );

  /**
   * Checks whether an observation of a station with exactly the given observation time is
   * already stored.
   *
   * <p>Matching is by equality of both values; no other fields are compared.
   *
   * @param wmoCode WMO code of the station, must not be {@code null}
   * @param observationTime exact observation timestamp, must not be {@code null}
   * @return {@code true} if at least one matching record exists, otherwise {@code false}
   */
  boolean existsByWmoCodeAndObservationTime(Integer wmoCode, Instant observationTime);

  /**
   * Finds stored observations of a station, page by page.
   *
   * <p>The order of records is defined solely by the sort of the supplied {@link Pageable}.
   * If the {@code Pageable} carries no sort, the order is not defined and pages are not
   * guaranteed to be consistent between calls.
   *
   * @param wmoCode WMO code of the station, must not be {@code null}
   * @param pageable page number, page size and sort, must not be {@code null}
   * @return the requested page; empty if the station has no observations or the page is
   *     beyond the last one, never {@code null}
   */
  Page<WeatherData> findByWmoCode(Integer wmoCode, Pageable pageable);
}
