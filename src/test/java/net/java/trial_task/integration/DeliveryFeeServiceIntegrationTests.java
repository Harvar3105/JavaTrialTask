package net.java.trial_task.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.domain.entity.condition.RBF;
import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
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
import net.java.trial_task.application.service.DeliveryFeeService;
import net.java.trial_task.shared.domain.entity.WeatherData;
import net.java.trial_task.shared.exceptions.WeatherDataNotFoundException;
import net.java.trial_task.shared.repository.WeatherRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DeliveryFeeServiceIntegrationTests {

  private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

  @Autowired DeliveryFeeService service;
  @Autowired CityRepository cityRepository;
  @Autowired VehicleRepository vehicleRepository;
  @Autowired RBFRepository rbfRepository;
  @Autowired ATEFRepository atefRepository;
  @Autowired WSEFRepository wsefRepository;
  @Autowired WPEFRepository wpefRepository;
  @Autowired WeatherRepository weatherRepository;

  private City tallinn;
  private City tartu;
  private Vehicle bike;
  private Vehicle car;

  @BeforeEach
  void setUp() {
    tallinn = cityRepository.saveAndFlush(new City("Tallinn"));
    tartu = cityRepository.saveAndFlush(new City("Tartu"));
    bike = vehicleRepository.saveAndFlush(new Vehicle("Bike"));
    car = vehicleRepository.saveAndFlush(new Vehicle("Car"));

    saveRbf(tallinn, bike, "3.0");
    saveRbf(tallinn, car, "4.0");
    saveRbf(tartu, bike, "2.5");
  }

  // ---------- ATEF: every Condition value, boundaries ----------

  @ParameterizedTest(name = "ATEF {0}: bound={1}, max={2}, observed={3} -> hit={4}")
  @CsvSource(nullValues = "null", value = {
    // less
    "less,           0,   null, -0.1, true",
    "less,           0,   null,  0.0, false",
    "less,           0,   null,  0.1, false",
    // greater
    "greater,        0,   null,  0.1, true",
    "greater,        0,   null,  0.0, false",
    "greater,        0,   null, -0.1, false",
    // equal (compareTo: scale-insensitive)
    "equal,         -5,   null, -5.0, true",
    "equal,         -5.0, null, -5.00, true",
    "equal,         -5,   null, -5.1, false",
    // lessOrEqual
    "lessOrEqual,    0,   null, -0.1, true",
    "lessOrEqual,    0,   null,  0.0, true",
    "lessOrEqual,    0,   null,  0.1, false",
    // greaterOrEqual
    "greaterOrEqual, 0,   null,  0.1, true",
    "greaterOrEqual, 0,   null,  0.0, true",
    "greaterOrEqual, 0,   null, -0.1, false",
    // inRange (inclusive)
    "inRange,       -10,  0,   -10.0, true",
    "inRange,       -10,  0,    -5.0, true",
    "inRange,       -10,  0,     0.0, true",
    "inRange,       -10,  0,   -10.1, false",
    "inRange,       -10,  0,     0.1, false",
    // notInRange (complement of inRange)
    "notInRange,    -10,  0,   -10.1, true",
    "notInRange,    -10,  0,     0.1, true",
    "notInRange,    -10,  0,   -10.0, false",
    "notInRange,    -10,  0,    -5.0, false",
    "notInRange,    -10,  0,     0.0, false"
  })
  void atef_condition(Condition condition, String bound, String max, String observed, boolean hit) {
    saveAtef(condition, bound, max, "1.0", bike);
    saveWeather("Tallinn-Harku", observed, "0.0", null, T0);

    BigDecimal fee = service.calculateDeliveryFee("Tallinn", "Bike", null);

    assertThat(fee).isEqualByComparingTo(hit ? "4.0" : "3.0");
  }

  // ---------- WSEF: every Condition value, boundaries ----------

  @ParameterizedTest(name = "WSEF {0}: bound={1}, max={2}, observed={3} -> hit={4}")
  @CsvSource(nullValues = "null", value = {
    "less,           10,  null,  9.9, true",
    "less,           10,  null, 10.0, false",
    "greater,        20,  null, 20.1, true",
    "greater,        20,  null, 20.0, false",
    "equal,          15,  null, 15.0, true",
    "equal,          15,  null, 15.1, false",
    "lessOrEqual,    10,  null, 10.0, true",
    "lessOrEqual,    10,  null, 10.1, false",
    "greaterOrEqual, 10,  null, 10.0, true",
    "greaterOrEqual, 10,  null,  9.9, false",
    "inRange,        10,  20,   10.0, true",
    "inRange,        10,  20,   20.0, true",
    "inRange,        10,  20,    9.9, false",
    "inRange,        10,  20,   20.1, false",
    "notInRange,     10,  20,    9.9, true",
    "notInRange,     10,  20,   20.1, true",
    "notInRange,     10,  20,   10.0, false",
    "notInRange,     10,  20,   20.0, false"
  })
  void wsef_condition(Condition condition, String bound, String max, String observed, boolean hit) {
    saveWsef(condition, bound, max, "0.5", bike);
    saveWeather("Tallinn-Harku", "5.0", observed, null, T0);

    BigDecimal fee = service.calculateDeliveryFee("Tallinn", "Bike", null);

    assertThat(fee).isEqualByComparingTo(hit ? "3.5" : "3.0");
  }

  // ---------- Combined rules ----------

  @Test
  void taskExample_tartuBike_sumsAllExtraFees() {
    saveAtef(Condition.inRange, "-10", "0", "0.5", bike);
    saveWsef(Condition.inRange, "10", "20", "0.5", bike);
    saveWpef("snow", "1.0", bike);
    saveWeather("Tartu-Tõravere", "-2.1", "4.7", "Light snow shower", T0);

    assertThat(service.calculateDeliveryFee("Tartu", "Bike", null)).isEqualByComparingTo("4.0");
  }

  @Test
  void atef_multipleMatchingRules_areSummed() {
    saveAtef(Condition.less, "0", null, "0.5", bike);
    saveAtef(Condition.less, "-10", null, "1.0", bike);
    saveWeather("Tallinn-Harku", "-15.0", "0.0", null, T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("4.5");
  }

  @Test
  void wpef_multipleMatchingRules_areSummed() {
    saveWpef("snow", "1.0", bike);
    saveWpef("shower", "0.5", bike);
    saveWeather("Tallinn-Harku", "5.0", "0.0", "Light snow shower", T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("4.5");
  }

  @Test
  void rule_withZeroFee_addsNothing() {
    saveAtef(Condition.less, "0", null, "0.0", bike);
    saveWeather("Tallinn-Harku", "-5.0", "0.0", null, T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("3.0");
  }

  // ---------- Vehicle binding ----------

  @Test
  void rule_boundToOtherVehicle_isIgnored() {
    saveAtef(Condition.less, "0", null, "1.0", bike);
    saveWsef(Condition.greater, "5", null, "1.0", bike);
    saveWpef("snow", "1.0", bike);
    saveWeather("Tallinn-Harku", "-5.0", "10.0", "Snow", T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Car", null)).isEqualByComparingTo("4.0");
  }

  @Test
  void rule_boundToMultipleVehicles_appliesToEach() {
    saveAtef(Condition.less, "0", null, "1.0", bike, car);
    saveWeather("Tallinn-Harku", "-5.0", "0.0", null, T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("4.0");
    assertThat(service.calculateDeliveryFee("Tallinn", "Car", null)).isEqualByComparingTo("5.0");
  }

  // ---------- Forbidden (fee = null) ----------

  @Test
  void rbf_nullFee_isForbidden() {
    saveRbf(tartu, car, null);
    saveWeather("Tartu-Tõravere", "5.0", "0.0", null, T0);

    assertThatThrownBy(() -> service.calculateDeliveryFee("Tartu", "Car", null))
      .isInstanceOf(VehicleUsageForbiddenException.class);
  }

  @Test
  void atef_forbiddenRule_matched_throws() {
    saveAtef(Condition.less, "-20", null, null, bike);
    saveWeather("Tallinn-Harku", "-25.0", "0.0", null, T0);

    assertThatThrownBy(() -> service.calculateDeliveryFee("Tallinn", "Bike", null))
      .isInstanceOf(VehicleUsageForbiddenException.class);
  }

  @Test
  void atef_forbiddenRule_notMatched_doesNotThrow() {
    saveAtef(Condition.less, "-20", null, null, bike);
    saveWeather("Tallinn-Harku", "-5.0", "0.0", null, T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("3.0");
  }

  @Test
  void wsef_forbiddenRule_matched_throws() {
    saveWsef(Condition.greater, "20", null, null, bike);
    saveWeather("Tallinn-Harku", "5.0", "20.1", null, T0);

    assertThatThrownBy(() -> service.calculateDeliveryFee("Tallinn", "Bike", null))
      .isInstanceOf(VehicleUsageForbiddenException.class);
  }

  @Test
  void wsef_forbiddenRule_boundary_notMatched_doesNotThrow() {
    saveWsef(Condition.greater, "20", null, null, bike);
    saveWeather("Tallinn-Harku", "5.0", "20.0", null, T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("3.0");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Glaze", "Hail", "Thunder", "Thunderstorm with hail"})
  void wpef_forbiddenPhenomenon_throws(String phenomenon) {
    saveWpef("glaze", null, bike);
    saveWpef("hail", null, bike);
    saveWpef("thunder", null, bike);
    saveWeather("Tallinn-Harku", "5.0", "0.0", phenomenon, T0);

    assertThatThrownBy(() -> service.calculateDeliveryFee("Tallinn", "Bike", null))
      .isInstanceOf(VehicleUsageForbiddenException.class);
  }

  @Test
  void forbiddenRule_forOtherVehicle_doesNotAffectCar() {
    saveWpef("hail", null, bike);
    saveWeather("Tallinn-Harku", "5.0", "0.0", "Hail", T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Car", null)).isEqualByComparingTo("4.0");
  }

  // ---------- Null / blank weather values ----------

  @Test
  void nullTemperatureAndWind_contributeZero() {
    saveAtef(Condition.less, "100", null, "1.0", bike);
    saveWsef(Condition.less, "100", null, "1.0", bike);
    saveWeather("Tallinn-Harku", null, null, null, T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("3.0");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  void blankPhenomenon_contributesZero(String phenomenon) {
    saveWpef("snow", "1.0", bike);
    saveWeather("Tallinn-Harku", "5.0", "0.0", phenomenon, T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("3.0");
  }

  // ---------- Phenomenon matching ----------

  @ParameterizedTest
  @CsvSource({
    "snow,  Light snow shower, true",
    "SNOW,  light snow shower, true",
    "snow,  Heavy SNOWFALL,    true",
    "rain,  Light snow shower, false",
    "snow,  Clear,             false"
  })
  void wpef_matching_isCaseInsensitiveSubstring(String ruleText, String observed, boolean hit) {
    saveWpef(ruleText, "1.0", bike);
    saveWeather("Tallinn-Harku", "5.0", "0.0", observed, T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null))
      .isEqualByComparingTo(hit ? "4.0" : "3.0");
  }

  // ---------- Weather selection: city, latest, datetime ----------

  @Test
  void weather_isSelectedByCityStation() {
    saveAtef(Condition.less, "0", null, "1.0", bike);
    saveWeather("Tallinn-Harku", "5.0", "0.0", null, T0);
    saveWeather("Tartu-Tõravere", "-5.0", "0.0", null, T0);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("3.0");
    assertThat(service.calculateDeliveryFee("Tartu", "Bike", null)).isEqualByComparingTo("3.5");
  }

  @Test
  void withoutDatetime_usesLatestObservation() {
    saveAtef(Condition.less, "0", null, "1.0", bike);
    saveWeather("Tallinn-Harku", "-5.0", "0.0", null, T0);
    saveWeather("Tallinn-Harku", "5.0", "0.0", null, T0.plus(1, ChronoUnit.HOURS));

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", null)).isEqualByComparingTo("3.0");
  }

  @Test
  void withDatetime_usesLatestObservationNotAfterThatTime() {
    saveAtef(Condition.less, "0", null, "1.0", bike);
    saveWeather("Tallinn-Harku", "-5.0", "0.0", null, T0);
    saveWeather("Tallinn-Harku", "5.0", "0.0", null, T0.plus(2, ChronoUnit.HOURS));

    Instant between = T0.plus(1, ChronoUnit.HOURS);

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", between)).isEqualByComparingTo("4.0");
  }

  @Test
  void withDatetime_equalToObservationTime_isInclusive() {
    saveAtef(Condition.less, "0", null, "1.0", bike);
    saveWeather("Tallinn-Harku", "-5.0", "0.0", null, T0);
    saveWeather("Tallinn-Harku", "5.0", "0.0", null, T0.plus(1, ChronoUnit.HOURS));

    assertThat(service.calculateDeliveryFee("Tallinn", "Bike", T0.plus(1, ChronoUnit.HOURS)))
      .isEqualByComparingTo("3.0");
  }

  @Test
  void withDatetime_beforeFirstObservation_throwsWeatherNotFound() {
    saveWeather("Tallinn-Harku", "5.0", "0.0", null, T0);
    assertThat(weatherRepository.findAll()).hasSize(1);

    assertThatThrownBy(() -> service.calculateDeliveryFee("Tallinn", "Bike", T0.minusSeconds(1)))
      .isInstanceOf(WeatherDataNotFoundException.class);
  }

  @Test
  void noWeatherForCity_throwsWeatherNotFound() {
    saveWeather("Tartu-Tõravere", "5.0", "0.0", null, T0);
    assertThat(weatherRepository.findAll()).hasSize(1);

    assertThatThrownBy(() -> service.calculateDeliveryFee("Tallinn", "Bike", null))
      .isInstanceOf(WeatherDataNotFoundException.class);
  }

  // ---------- Lookup errors ----------

  @Test
  void unknownCity_throwsCityNotFound() {
    assertThatThrownBy(() -> service.calculateDeliveryFee("Narva", "Bike", null))
      .isInstanceOf(CityNotFoundException.class);
  }

  @Test
  void unknownVehicle_throwsVehicleNotFound() {
    assertThatThrownBy(() -> service.calculateDeliveryFee("Tallinn", "Boat", null))
      .isInstanceOf(VehicleNotFoundException.class);
  }

  @Test
  void missingRbf_throwsRbfNotFound() {
    saveWeather("Tartu-Tõravere", "5.0", "0.0", null, T0);

    assertThatThrownBy(() -> service.calculateDeliveryFee("Tartu", "Car", null))
      .isInstanceOf(RBFNotFoundException.class);
  }

  // ---------- Helpers ----------

  private void saveRbf(City city, Vehicle vehicle, String fee) {
    rbfRepository.saveAndFlush(new RBF(city.getId(), vehicle.getId(), decimal(fee)));
  }

  private void saveAtef(Condition c, String bound, String max, String fee, Vehicle... vehicles) {
    atefRepository.saveAndFlush(
      new ATEF(c, decimal(bound), decimal(max), decimal(fee), Set.of(vehicles)));
  }

  private void saveWsef(Condition c, String bound, String max, String fee, Vehicle... vehicles) {
    wsefRepository.saveAndFlush(
      new WSEF(c, decimal(bound), decimal(max), decimal(fee), Set.of(vehicles)));
  }

  private void saveWpef(String phenomenon, String fee, Vehicle... vehicles) {
    wpefRepository.saveAndFlush(
      new WPEF(Condition.related, phenomenon, decimal(fee), Set.of(vehicles)));
  }

  private void saveWeather(String station, String temp, String wind, String phenomenon, Instant time) {
    // Adjust to the actual WeatherData constructor / setters.
    weatherRepository.saveAndFlush(
      new WeatherData(station, 00000, decimal(temp), decimal(wind), phenomenon, time));
  }

  private static BigDecimal decimal(String value) {
    return value == null ? null : new BigDecimal(value);
  }
}
