package net.java.trial_task.unit;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.java.trial_task.weather.client.WeatherApiClient;
import net.java.trial_task.weather.config.WeatherProperties;
import net.java.trial_task.weather.exceptions.WeatherDataUnavailableException;

@SpringBootTest 
class WeatherApiClientTests {

  private static final URI URL = URI.create("https://example.test/observations.php");

  private MockRestServiceServer server;
  private WeatherApiClient client;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    var properties = new WeatherProperties(URL, Duration.ofSeconds(1), Duration.ofSeconds(1), "-");
    client = new WeatherApiClient(builder.build(), properties);
  }

  @Test
  void returnsResponseBody() {
    server.expect(requestTo(URL))
      .andExpect(method(HttpMethod.GET))
      .andRespond(withSuccess("<observations/>", MediaType.APPLICATION_XML));

    byte[] body = client.fetchObservationsXml();

    assertThat(new String(body, StandardCharsets.UTF_8)).isEqualTo("<observations/>");
    server.verify();
  }

  @Test
  void serverErrorThrowsUnavailable() {
    server.expect(requestTo(URL)).andRespond(withServerError());

    assertThatThrownBy(() -> client.fetchObservationsXml())
      .isInstanceOf(WeatherDataUnavailableException.class);
  }

  @Test
  void emptyBodyThrowsUnavailable() {
    server.expect(requestTo(URL)).andRespond(withSuccess("", MediaType.APPLICATION_XML));

    assertThatThrownBy(() -> client.fetchObservationsXml())
      .isInstanceOf(WeatherDataUnavailableException.class)
      .hasMessageContaining("Empty response");
  }
}
