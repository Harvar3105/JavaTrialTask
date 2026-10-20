package net.java.trial_task.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.domain.entity.condition.RBF;
import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.exceptions.DeliveryFeeException;
import net.java.trial_task.application.exceptions.condition.RBFNotFoundException;
import net.java.trial_task.application.exceptions.data.CityNotFoundException;
import net.java.trial_task.application.exceptions.data.VehicleNotFoundException;
import net.java.trial_task.application.exceptions.data.VehicleUsageForbiddenException;
import net.java.trial_task.application.repository.condition.ATEFRepository;
import net.java.trial_task.application.repository.condition.RBFRepository;
import net.java.trial_task.application.repository.condition.WPEFRepository;
import net.java.trial_task.application.repository.condition.WSEFRepository;
import net.java.trial_task.application.repository.data.CityRepository;
import net.java.trial_task.application.repository.data.VehicleRepository;
import net.java.trial_task.shared.domain.entity.WeatherData;
import net.java.trial_task.shared.exceptions.WeatherDataNotFoundException;
import net.java.trial_task.shared.repository.WeatherRepository;
import net.java.trial_task.application.util.ConditionEvaluator;

/**
 * Calculates the delivery fee for a city and vehicle type from the regional base fee and
 * weather-dependent extra fees.
 *
 * <p>Total fee = RBF (regional base fee) + ATEF (air temperature extra fee) + WSEF (wind
 * speed extra fee) + WPEF (weather phenomenon extra fee). The service only reads data; it
 * writes nothing and keeps no state.
 *
 * <p>The class is annotated with {@code @Transactional(readOnly = true)}, so
 * {@link #calculateDeliveryFee(String, String, Instant)} runs in one read-only transaction
 * (or joins an active one) and all repository reads of a calculation take place within it.
 * Whether those reads see one consistent snapshot depends on the isolation level of the
 * database, which is not defined by this class.
 *
 * <p>Calculation order, which also determines which error is reported when several apply:
 * <ol>
 *   <li>the city and the vehicle are looked up by exact name;</li>
 *   <li>RBF is determined for the city and vehicle pair;</li>
 *   <li>the weather observation is selected for the city;</li>
 *   <li>ATEF, WSEF and WPEF are determined in this order from that observation.</li>
 * </ol>
 * The first failing stage aborts the calculation.
 *
 * <p>A {@code null} fee in RBF, ATEF, WSEF or WPEF means that usage of the vehicle is
 * forbidden and causes {@link VehicleUsageForbiddenException}. Extra fee rules apply to a
 * vehicle only if the vehicle is in the rule's vehicle set; this is how extra fees are
 * restricted to specific vehicle types.
 *
 * <p>Calls to {@link WeatherRepository} are intercepted by {@link WeatherRepositoryAspect}:
 * a {@link org.springframework.dao.DataAccessException} raised by them is reported as
 * {@link WeatherDataRepositoryUnavailableException}.
 */
@Service
@Transactional(readOnly = true)
public class DeliveryFeeService {

  private final CityRepository cityRepository;
  private final VehicleRepository vehicleRepository;
  private final RBFRepository rbfRepository;
  private final ATEFRepository atefRepository;
  private final WSEFRepository wsefRepository;
  private final WPEFRepository wpefRepository;
  private final WeatherRepository weatherRepository;
  private static final Logger log = LoggerFactory.getLogger(DeliveryFeeService.class);

  /**
   * Creates the service.
   *
   * @param cityRepository repository of cities, must not be {@code null}
   * @param vehicleRepository repository of vehicles, must not be {@code null}
   * @param rbfRepository repository of regional base fees, must not be {@code null}
   * @param atefRepository repository of air temperature rules, must not be {@code null}
   * @param wsefRepository repository of wind speed rules, must not be {@code null}
   * @param wpefRepository repository of weather phenomenon rules, must not be {@code null}
   * @param weatherRepository repository of weather observations, must not be {@code null}
   */
  public DeliveryFeeService(
    CityRepository cityRepository,
    VehicleRepository vehicleRepository,
    RBFRepository rbfRepository,
    ATEFRepository atefRepository,
    WSEFRepository wsefRepository,
    WPEFRepository wpefRepository,
    WeatherRepository weatherRepository
  ) {
    this.cityRepository = cityRepository;
    this.vehicleRepository = vehicleRepository;
    this.rbfRepository = rbfRepository;
    this.atefRepository = atefRepository;
    this.wsefRepository = wsefRepository;
    this.wpefRepository = wpefRepository;
    this.weatherRepository = weatherRepository;
  }

  /**
   * Calculates the total delivery fee as RBF + ATEF + WSEF + WPEF.
   *
   * <p>The city and the vehicle are found by exact name match as stored in the {@code city}
   * and {@code vehicle} tables; case sensitivity follows the database comparison rules. The
   * weather observation used for the extra fees is the latest one for the city or, if
   * {@code at} is given, the latest one made at or before {@code at} (inclusive upper bound,
   * no lower bound, so the observation may be arbitrarily old). The observation of a city is
   * selected by case-insensitive substring match of the city name against the station name.
   * If several stations match, the observation with the latest observation time among all of
   * them is used; the choice between equal times is not defined.
   *
   * <p>Extra fee rules:
   * <ul>
   *   <li>for each stage, all rules of the vehicle that match the observed value are
   *       selected and their fees are summed; a rule that does not match contributes
   *       nothing;</li>
   *   <li>if the observed value of a stage is {@code null} (for the phenomenon: {@code null}
   *       or blank), the stage contributes 0 and no rule is evaluated, so a
   *       forbidden-usage rule cannot trigger for a missing value;</li>
   *   <li>ATEF and WSEF rules are matched by {@link ConditionEvaluator} using the rule's
   *       condition and thresholds;</li>
   *   <li>WPEF rules are matched when the observed phenomenon contains the rule's
   *       phenomenon as a substring, ignoring case; the rule's condition is not used;</li>
   *   <li>a matching rule with a {@code null} fee forbids usage of the vehicle.</li>
   * </ul>
   *
   * <p>The result is the sum as {@link BigDecimal}; its scale is not normalized. On
   * successful completion the method logs the inputs and stage results at INFO level; when
   * a stage fails, nothing is logged by this method.
   *
   * @param cityName city name as stored in the city table, must not be {@code null}
   * @param vehicleType vehicle type as stored in the vehicle table, must not be {@code null}
   * @param at moment for which weather data is evaluated; {@code null} means the latest
   *     data
   * @return total delivery fee, never {@code null}
   * @throws CityNotFoundException if no city with the given name exists
   * @throws VehicleNotFoundException if no vehicle with the given type exists
   * @throws RBFNotFoundException if no regional base fee is defined for the pair
   * @throws WeatherDataNotFoundException if no matching observation exists for the city (and
   *     for {@code at}, if given)
   * @throws VehicleUsageForbiddenException if the RBF or any matching extra fee rule has a
   *     {@code null} fee
   * @throws WeatherDataRepositoryUnavailableException if reading weather observations fails
   *     with a persistence error
   * @throws DeliveryFeeException if an entity or data is missing, or vehicle usage is
   *     forbidden (common type of the business exceptions above, if they extend it)
   */
  public BigDecimal calculateDeliveryFee(String cityName, String vehicleType, Instant at) {
    City city = cityRepository.getByCityName(cityName)
      .orElseThrow(() -> new CityNotFoundException(cityName));
    Vehicle vehicle = vehicleRepository.getByVehicleType(vehicleType)
      .orElseThrow(() -> new VehicleNotFoundException(vehicleType));

    BigDecimal rbf = calculateRbf(city, vehicle);
    WeatherData weather = findWeather(city, at);

    BigDecimal atef = calculateAtef(vehicle, weather);
    BigDecimal wsef = calculateWsef(vehicle, weather);
    BigDecimal wpef = calculateWpef(vehicle, weather);

    log.info("City name: " + city.getCityName());
    log.info("Vehicle name: " + vehicle.getVehicleType());
    log.info("City name: " + city.getCityName());
    log.info("RBF: " + rbf);
    log.info("weather name: " + weather.getStation());
    log.info("atef: " + atef);
    log.info("wsef: " + wsef);
    log.info("wpef: " + wpef);

    return rbf.add(atef).add(wsef).add(wpef);
  }

  /**
   * Determines the regional base fee for the city and vehicle pair.
   *
   * @param city the city, must not be {@code null}
   * @param vehicle the vehicle, must not be {@code null}
   * @return the base fee, never {@code null}
   * @throws RBFNotFoundException if no record exists for the pair
   * @throws VehicleUsageForbiddenException if the record's fee is {@code null}
   */
  private BigDecimal calculateRbf(City city, Vehicle vehicle) {
    RBF rbf = rbfRepository.findByCityIdAndVehicleTypeId(city.getId(), vehicle.getId())
      .orElseThrow(() -> new RBFNotFoundException(city.getCityName(), vehicle.getVehicleType()));
    return requireAllowed(rbf.getFee());
  }

  /**
   * Selects the weather observation for the city.
   *
   * <p>Stations are matched by case-insensitive substring of the city name. With
   * {@code at == null} the latest observation is returned; otherwise the latest observation
   * made at or before {@code at}.
   *
   * @param city the city, must not be {@code null}
   * @param at moment for which the observation is requested, {@code null} for the latest
   * @return the selected observation, never {@code null}
   * @throws WeatherDataNotFoundException if no matching observation exists
   */
  private WeatherData findWeather(City city, Instant at) {
    String cityName = city.getCityName();
    Optional<WeatherData> weather = (at == null)
      ? weatherRepository.findFirstByStationContainingIgnoreCaseOrderByObservationTimeDesc(cityName)
      : weatherRepository
          .findFirstByStationContainingIgnoreCaseAndObservationTimeLessThanEqualOrderByObservationTimeDesc(cityName, at);
    return weather.orElseThrow(() -> new WeatherDataNotFoundException(cityName, at));
  }

  /**
   * Determines the air temperature extra fee.
   *
   * @param vehicle the vehicle whose rules are evaluated, must not be {@code null}
   * @param weather the observation providing the air temperature, must not be {@code null}
   * @return the sum of fees of all matching rules; 0 if the observed temperature is
   *     {@code null} or no rule matches
   * @throws VehicleUsageForbiddenException if a matching rule has a {@code null} fee
   */
  private BigDecimal calculateAtef(Vehicle vehicle, WeatherData weather) {
    BigDecimal temperature = weather.getAirTemperature();
    if (temperature == null) return BigDecimal.ZERO;

    List<BigDecimal> fees = atefRepository.findAllByVehicles_Id(vehicle.getId()).stream()
      .filter(rule -> ConditionEvaluator.matches(
        rule.getCondition(), temperature, rule.getTemperature(), rule.getMaxTemperature()))
      .map(ATEF::getFee)
      .toList();
    return sumAllowed(fees);
  }

  /**
   * Determines the wind speed extra fee.
   *
   * @param vehicle the vehicle whose rules are evaluated, must not be {@code null}
   * @param weather the observation providing the wind speed, must not be {@code null}
   * @return the sum of fees of all matching rules; 0 if the observed wind speed is
   *     {@code null} or no rule matches
   * @throws VehicleUsageForbiddenException if a matching rule has a {@code null} fee
   */
  private BigDecimal calculateWsef(Vehicle vehicle, WeatherData weather) {
    BigDecimal windSpeed = weather.getWindSpeed();
    if (windSpeed == null) return BigDecimal.ZERO;

    List<BigDecimal> fees = wsefRepository.findAllByVehicles_Id(vehicle.getId()).stream()
      .filter(rule -> ConditionEvaluator.matches(
        rule.getCondition(), windSpeed, rule.getWindSpeed(), rule.getMaxWindSpeed()))
      .map(WSEF::getFee)
      .toList();
    return sumAllowed(fees);
  }

  /**
   * Determines the weather phenomenon extra fee.
   *
   * <p>A rule matches if the observed phenomenon contains the rule's phenomenon as a
   * substring, ignoring case. The rule's condition is not evaluated. A rule whose
   * phenomenon is {@code null} causes a {@link NullPointerException}.
   *
   * @param vehicle the vehicle whose rules are evaluated, must not be {@code null}
   * @param weather the observation providing the phenomenon, must not be {@code null}
   * @return the sum of fees of all matching rules; 0 if the observed phenomenon is
   *     {@code null} or blank, or no rule matches
   * @throws VehicleUsageForbiddenException if a matching rule has a {@code null} fee
   */
  private BigDecimal calculateWpef(Vehicle vehicle, WeatherData weather) {
    String phenomenon = weather.getPhenomenon();
    if (phenomenon == null || phenomenon.isBlank()) return BigDecimal.ZERO;

    String observed = phenomenon.toLowerCase(Locale.ROOT);
    List<BigDecimal> fees = wpefRepository.findAllByVehicles_Id(vehicle.getId()).stream()
      .filter(rule -> observed.contains(rule.getPhenomenon().toLowerCase(Locale.ROOT)))
      .map(WPEF::getFee)
      .toList();
    return sumAllowed(fees);
  }

  /**
   * Sums the given fees, rejecting forbidden ones.
   *
   * @param fees fees to sum; elements may be {@code null}
   * @return the sum, 0 for an empty list
   * @throws VehicleUsageForbiddenException if any element is {@code null}
   */
  private BigDecimal sumAllowed(List<BigDecimal> fees) {
    BigDecimal total = BigDecimal.ZERO;
    for (BigDecimal fee : fees) {
      total = total.add(requireAllowed(fee));
    }
    return total;
  }

  /**
   * Checks that a fee permits vehicle usage.
   *
   * @param fee the fee to check, {@code null} means usage is forbidden
   * @return the same fee, never {@code null}
   * @throws VehicleUsageForbiddenException if {@code fee} is {@code null}
   */
  private BigDecimal requireAllowed(BigDecimal fee) {
    if (fee == null) throw new VehicleUsageForbiddenException();
    return fee;
  }
}
