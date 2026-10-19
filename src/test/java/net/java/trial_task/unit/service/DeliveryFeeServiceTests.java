package net.java.trial_task.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
import net.java.trial_task.application.exceptions.data.WeatherDataNotFoundException;
import net.java.trial_task.application.repository.condition.ATEFRepository;
import net.java.trial_task.application.repository.condition.RBFRepository;
import net.java.trial_task.application.repository.condition.WPEFRepository;
import net.java.trial_task.application.repository.condition.WSEFRepository;
import net.java.trial_task.application.repository.data.CityRepository;
import net.java.trial_task.application.repository.data.VehicleRepository;
import net.java.trial_task.application.service.DeliveryFeeService;
import net.java.trial_task.shared.domain.entity.WeatherData;
import net.java.trial_task.shared.repository.WeatherRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeliveryFeeServiceTests {

  private static final String CITY_NAME = "Tartu";
  private static final String VEHICLE_TYPE = "Bike";
  private static final Long CITY_ID = 1L;
  private static final Long VEHICLE_ID = 2L;

  @Mock private CityRepository cityRepository;
  @Mock private VehicleRepository vehicleRepository;
  @Mock private RBFRepository rbfRepository;
  @Mock private ATEFRepository atefRepository;
  @Mock private WSEFRepository wsefRepository;
  @Mock private WPEFRepository wpefRepository;
  @Mock private WeatherRepository weatherRepository;

  @Mock private City city;
  @Mock private Vehicle vehicle;
  @Mock private RBF rbf;

  private DeliveryFeeService service;

  @BeforeEach
  void setUp() {
    service = new DeliveryFeeService(
      cityRepository, vehicleRepository, rbfRepository,
      atefRepository, wsefRepository, wpefRepository, weatherRepository
    );

    when(city.getId()).thenReturn(CITY_ID);
    when(city.getCityName()).thenReturn(CITY_NAME);
    when(vehicle.getId()).thenReturn(VEHICLE_ID);
    when(vehicle.getVehicleType()).thenReturn(VEHICLE_TYPE);
    when(rbf.getFee()).thenReturn(bd("2.5"));

    when(cityRepository.getByCityName(CITY_NAME)).thenReturn(Optional.of(city));
    when(vehicleRepository.getByVehicleType(VEHICLE_TYPE)).thenReturn(Optional.of(vehicle));
    when(rbfRepository.findByCityIdAndVehicleTypeId(CITY_ID, VEHICLE_ID)).thenReturn(Optional.of(rbf));

    when(atefRepository.findAllByVehicles_Id(VEHICLE_ID)).thenReturn(List.of());
    when(wsefRepository.findAllByVehicles_Id(VEHICLE_ID)).thenReturn(List.of());
    when(wpefRepository.findAllByVehicles_Id(VEHICLE_ID)).thenReturn(List.of());

    stubLatestWeather(weather("0.0", "0.0", "Clear"));
  }

  @Test
  void calculate_allStagesMatch_returnsSum() {
    stubLatestWeather(weather("-2.1", "4.7", "Light snow shower"));
    when(atefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new ATEF(Condition.inRange, bd("-10.0"), bd("0.0"), bd("0.5"), null)));
    when(wsefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new WSEF(Condition.inRange, bd("10.0"), bd("20.0"), bd("0.5"), Set.of())));
    when(wpefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new WPEF(Condition.related, "snow", bd("1.0"), Set.of())));

    BigDecimal result = service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null);

    assertThat(result).isEqualByComparingTo("4.0");
  }

  @Test
  void calculate_noWeatherRules_returnsRbfOnly() {
    BigDecimal result = service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null);

    assertThat(result).isEqualByComparingTo("2.5");
  }

  @Test
  void calculate_ruleConditionNotMet_ruleIgnored() {
    stubLatestWeather(weather("5.0", "4.7", "Clear"));
    when(atefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new ATEF(Condition.less, bd("0.0"), null, bd("0.5"), null)));

    BigDecimal result = service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null);

    assertThat(result).isEqualByComparingTo("2.5");
  }

  @Test
  void calculate_nullWeatherMeasurements_stagesGiveZero() {
    stubLatestWeather(new WeatherData("Tartu-Tõravere", 26242, null, null, null, Instant.now()));
    when(atefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new ATEF(Condition.less, bd("0.0"), null, null, null)));
    when(wsefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new WSEF(Condition.greater, bd("20.0"), null, null, Set.of())));
    when(wpefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new WPEF(Condition.related, "hail", null, Set.of())));

    BigDecimal result = service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null);

    assertThat(result).isEqualByComparingTo("2.5");
  }

  @Test
  void calculate_multipleMatchingRulesInStage_feesAreSummed() {
    stubLatestWeather(weather("-15.0", "4.7", "Clear"));
    when(atefRepository.findAllByVehicles_Id(VEHICLE_ID)).thenReturn(List.of(
      new ATEF(Condition.less, bd("0.0"), null, bd("0.5"), null),
      new ATEF(Condition.less, bd("-10.0"), null, bd("1.0"), null)
    ));

    BigDecimal result = service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null);

    assertThat(result).isEqualByComparingTo("4.0");
  }

  @Test
  void calculate_atInstantProvided_usesTimeBoundedWeatherQuery() {
    Instant at = Instant.parse("2026-10-01T10:00:00Z");
    when(weatherRepository
      .findFirstByStationContainingIgnoreCaseAndObservationTimeLessThanEqualOrderByObservationTimeDesc(CITY_NAME, at))
      .thenReturn(Optional.of(weather("-2.1", "4.7", "Clear")));
    when(atefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new ATEF(Condition.inRange, bd("-10.0"), bd("0.0"), bd("0.5"), null)));

    BigDecimal result = service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, at);

    assertThat(result).isEqualByComparingTo("3.0");
    verify(weatherRepository, never())
      .findFirstByStationContainingIgnoreCaseOrderByObservationTimeDesc(anyString());
  }

  @Test
  void calculate_atInstantNull_usesLatestWeatherQuery() {
    service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null);

    verify(weatherRepository).findFirstByStationContainingIgnoreCaseOrderByObservationTimeDesc(CITY_NAME);
    verify(weatherRepository, never())
      .findFirstByStationContainingIgnoreCaseAndObservationTimeLessThanEqualOrderByObservationTimeDesc(
        anyString(), any(Instant.class));
  }

  @Test
  void calculate_cityNotFound_throwsAndStopsChain() {
    when(cityRepository.getByCityName("Unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.calculateDeliveryFee("Unknown", VEHICLE_TYPE, null))
      .isInstanceOf(CityNotFoundException.class);
    verifyNoInteractions(rbfRepository, weatherRepository, atefRepository, wsefRepository, wpefRepository);
  }

  @Test
  void calculate_vehicleNotFound_throwsAndStopsChain() {
    when(vehicleRepository.getByVehicleType("Plane")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.calculateDeliveryFee(CITY_NAME, "Plane", null))
      .isInstanceOf(VehicleNotFoundException.class);
    verifyNoInteractions(rbfRepository, weatherRepository, atefRepository, wsefRepository, wpefRepository);
  }

  @Test
  void calculate_rbfNotFound_throwsBeforeWeatherStages() {
    when(rbfRepository.findByCityIdAndVehicleTypeId(CITY_ID, VEHICLE_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null))
      .isInstanceOf(RBFNotFoundException.class);
    verifyNoInteractions(weatherRepository, atefRepository, wsefRepository, wpefRepository);
  }

  @Test
  void calculate_rbfFeeNull_throwsForbidden() {
    when(rbf.getFee()).thenReturn(null);

    assertThatThrownBy(() -> service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null))
      .isInstanceOf(VehicleUsageForbiddenException.class)
      .hasMessage("Usage of selected vehicle type is forbidden");
    verifyNoInteractions(weatherRepository);
  }

  @Test
  void calculate_weatherNotFound_throws() {
    when(weatherRepository.findFirstByStationContainingIgnoreCaseOrderByObservationTimeDesc(CITY_NAME))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null))
      .isInstanceOf(WeatherDataNotFoundException.class);
  }

  @Test
  void calculate_atefFeeNull_throwsForbidden() {
    stubLatestWeather(weather("-15.0", "4.7", "Clear"));
    // ATEF atefRule = atef(Condition.less, "-10.0", null, null);
    when(atefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new ATEF(Condition.less, bd("-10.0"), null, null, null)));

    assertThatThrownBy(() -> service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null))
      .isInstanceOf(VehicleUsageForbiddenException.class);
  }

  @Test
  void calculate_wsefFeeNull_throwsForbidden() {
    stubLatestWeather(weather("5.0", "25.0", "Clear"));
    when(wsefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new WSEF(Condition.greater, bd("20.0"), null, null, Set.of())));

    assertThatThrownBy(() -> service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null))
      .isInstanceOf(VehicleUsageForbiddenException.class);
  }

  @Test
  void calculate_wpefFeeNull_throwsForbidden() {
    stubLatestWeather(weather("5.0", "4.7", "Thunder"));
    when(wpefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new WPEF(Condition.related, "thunder", null, Set.of())));

    assertThatThrownBy(() -> service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null))
      .isInstanceOf(VehicleUsageForbiddenException.class);
  }

  @Test
  void calculate_wpefPhenomenonMatchIgnoresCase() {
    stubLatestWeather(weather("5.0", "4.7", "Light RAIN"));
    when(wpefRepository.findAllByVehicles_Id(VEHICLE_ID))
      .thenReturn(List.of(new WPEF(Condition.related, "rain", bd("0.5"), Set.of())));

    BigDecimal result = service.calculateDeliveryFee(CITY_NAME, VEHICLE_TYPE, null);

    assertThat(result).isEqualByComparingTo("3.0");
  }

  private void stubLatestWeather(WeatherData weather) {
    when(weatherRepository.findFirstByStationContainingIgnoreCaseOrderByObservationTimeDesc(CITY_NAME))
      .thenReturn(Optional.of(weather));
  }

  private static WeatherData weather(String temperature, String windSpeed, String phenomenon) {
    return new WeatherData("Tartu-Tõravere", 26242, bd(temperature), bd(windSpeed), phenomenon, Instant.now());
  }

  private static BigDecimal bd(String value) {
    return new BigDecimal(value);
  }
}
