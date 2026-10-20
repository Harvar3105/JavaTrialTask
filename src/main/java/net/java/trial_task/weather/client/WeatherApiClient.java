package net.java.trial_task.weather.client;


import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import net.java.trial_task.weather.config.WeatherProperties;
import net.java.trial_task.weather.exceptions.WeatherDataUnavailableException;

/**
 * Client for the remote weather observations source.
 *
 * <p>Performs a single HTTP GET request to the URI configured in
 * {@link WeatherProperties#sourceUri()} and returns the raw response body. The client does
 * not parse the response, does not persist anything and does not retry failed requests.
 * Connection and read timeouts are not applied by this class; they are determined by the
 * supplied {@link RestClient}.
 *
 * <p>The class holds no mutable state.
 */
@Component
public class WeatherApiClient {

  private final RestClient restClient;
  private final WeatherProperties properties;

  /**
   * Creates a client.
   *
   * @param weatherRestClient HTTP client used for requests to the weather source, must not be
   *     {@code null}; expected to be preconfigured with the connect and read timeouts
   * @param properties weather configuration providing the source URI, must not be {@code null}
   */
  public WeatherApiClient(RestClient weatherRestClient, WeatherProperties properties) {
    this.restClient = weatherRestClient;
    this.properties = properties;
  }

  /**
   * Fetches the current observations document from the weather source.
   *
   * <p>Sends one GET request to the configured source URI and returns the response body
   * unchanged, as bytes. The bytes are returned without charset decoding so that the XML
   * parser can apply the encoding declared in the document.
   *
   * <p>Any failure of the request is reported as {@link WeatherDataUnavailableException}.
   * The cause is preserved for {@link RestClientException} failures, which include:
   * <ul>
   *   <li>I/O errors, connection failures and timeouts;</li>
   *   <li>HTTP responses with a 4xx or 5xx status.</li>
   * </ul>
   * A successful response with a {@code null} or zero-length body is treated as a failure
   * as well, without a cause. The content of a non-empty body is not validated.
   *
   * @return the response body, never {@code null} and never empty
   * @throws WeatherDataUnavailableException if the request fails or the response body is
   *     {@code null} or empty
   */
  public byte[] fetchObservationsXml() {
    byte[] body;
    try {
      body = restClient.get()
        .uri(properties.sourceUri())
        .retrieve()
        .body(byte[].class);
    } catch (RestClientException e) {
      throw new WeatherDataUnavailableException("Failed to fetch weather data from " + properties.sourceUri(), e);
    }
    if (body == null || body.length == 0) {
      throw new WeatherDataUnavailableException("Empty response from " + properties.sourceUri());
    }
    return body;
  }
}
