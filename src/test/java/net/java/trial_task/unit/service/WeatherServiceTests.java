package net.java.trial_task.unit.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.java.trial_task.shared.domain.entity.WeatherData;
import net.java.trial_task.shared.repository.WeatherRepository;
import net.java.trial_task.shared.service.WeatherService;
import net.java.trial_task.weather.client.WeatherApiClient;
import net.java.trial_task.weather.dto.ObservationsDto;
import net.java.trial_task.weather.dto.StationDto;
import net.java.trial_task.weather.exceptions.WeatherDataUnavailableException;
import net.java.trial_task.weather.util.ObservationsXmlParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTests {

  @Mock private ObservationsXmlParser parser;
  @Mock private WeatherApiClient client;
  @Mock private WeatherRepository repo;

  private WeatherService service;

  @BeforeEach
  void setUp() {
    service = new WeatherService(parser, client, repo);
  }

  @Test
  void parseDtoMapsAllFields() {
    var dto = new ObservationsDto(1760000000L, List.of(new StationDto(
      "Tartu-Tõravere",
      "26242",
      "Light snow shower",
      -2.1,
      4.7
    )));

    var result = service.parseDto(dto);

    var expected = new WeatherData(
      "Tartu-Tõravere",
      26242,
      BigDecimal.valueOf(-2.1),
      BigDecimal.valueOf(4.7),
      "Light snow shower",
      Instant.ofEpochSecond(1760000000L)
    );
    assertThat(result).singleElement().usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  void parseDtoKeepsNullMeasurements() {
    var dto = new ObservationsDto(1L, List.of(new StationDto(
      "Pärnu",
      "41803",
      null,
      null,
      null
    )));

    var result = service.parseDto(dto);

    var expected = new WeatherData(
      "Pärnu",
      41803,
      null,
      null,
      null,
      Instant.ofEpochSecond(1L)
    );
    assertThat(result).singleElement().usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  void parseDtoSkipsStationWithoutName() {
    var dto = new ObservationsDto(1L, List.of(new StationDto(
      null,
      "1",
      null,
      1.0,
      1.0
    )));

    assertThat(service.parseDto(dto)).isEmpty();
  }

  @Test
  void saveWeatherDataSkipsRepositoryForEmptyCollection() {
    service.saveWeatherData(List.of());

    verifyNoInteractions(repo);
  }

  @Test
  void updateRunsFullPipeline() {
    byte[] xml = new byte[] {1};
    var dto = new ObservationsDto(1L, List.of(new StationDto(
      "Pärnu",
      "41803",
      null,
      1.0,
      2.0
    )));
    when(client.fetchObservationsXml()).thenReturn(xml);
    when(parser.parse(xml)).thenReturn(dto);

    service.updateWeatherAsync();

    verify(repo).saveAllAndFlush(argThat(c -> ((Collection<?>) c).size() == 1));
  }

  @Test
  void updateSwallowsExceptions() {
    when(client.fetchObservationsXml()).thenThrow(new WeatherDataUnavailableException("down"));

    assertThatCode(() -> service.updateWeatherAsync()).doesNotThrowAnyException();
    verifyNoInteractions(repo);
  }

  @Test
  void updateReleasesFlagAfterFailure() {
    when(client.fetchObservationsXml()).thenThrow(new WeatherDataUnavailableException("down"));

    service.updateWeatherAsync();
    service.updateWeatherAsync();

    verify(client, times(2)).fetchObservationsXml();
  }

  @Test
  void concurrentImportIsSkipped() throws Exception {
    var started = new CountDownLatch(1);
    var release = new CountDownLatch(1);
    when(client.fetchObservationsXml()).thenAnswer(inv -> {
      started.countDown();
      release.await(5, TimeUnit.SECONDS);
      throw new WeatherDataUnavailableException("stop");
    });

    ExecutorService pool = Executors.newSingleThreadExecutor();
    try {
      Future<?> first = pool.submit(() -> service.updateWeatherAsync());
      assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

      service.updateWeatherAsync();

      release.countDown();
      first.get(5, TimeUnit.SECONDS);
      verify(client, times(1)).fetchObservationsXml();
    } finally {
      pool.shutdownNow();
    }
  }
}
