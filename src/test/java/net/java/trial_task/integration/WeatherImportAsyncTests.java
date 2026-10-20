package net.java.trial_task.integration;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import net.java.trial_task.shared.repository.WeatherRepository;
import net.java.trial_task.shared.service.WeatherService;
import net.java.trial_task.weather.client.WeatherApiClient;
import net.java.trial_task.weather.exceptions.WeatherDataUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(properties = "weather.cron=-")
class WeatherImportAsyncTests {

  private static final String XML = """
    <?xml version="1.0" encoding="UTF-8"?>
    <observations timestamp="1760000000">
      <station>
        <name>Tallinn-Harku</name><wmocode>26038</wmocode>
        <phenomenon>Clear</phenomenon><airtemperature>3.5</airtemperature><windspeed>2.0</windspeed>
      </station>
    </observations>
    """;

  @Autowired private WeatherService service;
  @Autowired private WeatherRepository repo;

  @MockitoBean private WeatherApiClient client;

  @BeforeEach
  void clean() {
    repo.deleteAll();
  }

  @Test
  void importRunsOnSeparateThreadAndPersists() {
    var workerThread = new AtomicReference<String>();
    when(client.fetchObservationsXml()).thenAnswer(inv -> {
      workerThread.set(Thread.currentThread().getName());
      return XML.getBytes(StandardCharsets.UTF_8);
    });

    service.updateWeatherAsync();

    await().atMost(Duration.ofSeconds(5))
      .untilAsserted(() -> assertThat(repo.count()).isEqualTo(1));
    assertThat(workerThread.get()).isNotEqualTo(Thread.currentThread().getName());
  }

  @Test
  void failingSourceDoesNotPersistOrPropagate() {
    when(client.fetchObservationsXml()).thenThrow(new WeatherDataUnavailableException("down"));

    assertThatCode(() -> service.updateWeatherAsync()).doesNotThrowAnyException();

    verify(client, timeout(5000)).fetchObservationsXml();
    assertThat(repo.count()).isZero();
  }
}
