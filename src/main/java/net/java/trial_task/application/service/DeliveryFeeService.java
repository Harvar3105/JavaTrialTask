package net.java.trial_task.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
import net.java.trial_task.application.exceptions.data.WeatherDataNotFoundException;
import net.java.trial_task.application.repository.condition.ATEFRepository;
import net.java.trial_task.application.repository.condition.RBFRepository;
import net.java.trial_task.application.repository.condition.WPEFRepository;
import net.java.trial_task.application.repository.condition.WSEFRepository;
import net.java.trial_task.application.repository.data.CityRepository;
import net.java.trial_task.application.repository.data.VehicleRepository;
import net.java.trial_task.shared.domain.entity.WeatherData;
import net.java.trial_task.shared.repository.WeatherRepository;
import net.java.trial_task.application.util.ConditionEvaluator;

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
   * Stages run sequentially; the first failing stage aborts the calculation.
   *
   * @param cityName    city name as stored in the city table
   * @param vehicleType vehicle type as stored in the vehicle table
   * @param at          moment for which weather data is evaluated; null = latest data
   * @return total delivery fee
   * @throws DeliveryFeeException if an entity or data is missing, or vehicle usage is forbidden
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

    return rbf.add(atef).add(wsef).add(wpef);
  }

  private BigDecimal calculateRbf(City city, Vehicle vehicle) {
    RBF rbf = rbfRepository.findByCityIdAndVehicleTypeId(city.getId(), vehicle.getId())
      .orElseThrow(() -> new RBFNotFoundException(city.getCityName(), vehicle.getVehicleType()));
    return requireAllowed(rbf.getFee());
  }

  private WeatherData findWeather(City city, Instant at) {
    String cityName = city.getCityName();
    Optional<WeatherData> weather = (at == null)
      ? weatherRepository.findFirstByStationContainingIgnoreCaseOrderByObservationTimeDesc(cityName)
      : weatherRepository
          .findFirstByStationContainingIgnoreCaseAndObservationTimeLessThanEqualOrderByObservationTimeDesc(cityName, at);
    return weather.orElseThrow(() -> new WeatherDataNotFoundException(cityName, at));
  }

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

  private BigDecimal sumAllowed(List<BigDecimal> fees) {
    BigDecimal total = BigDecimal.ZERO;
    for (BigDecimal fee : fees) {
      total = total.add(requireAllowed(fee));
    }
    return total;
  }

  private BigDecimal requireAllowed(BigDecimal fee) {
    if (fee == null) throw new VehicleUsageForbiddenException();
    return fee;
  }
}
