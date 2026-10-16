package net.java.trial_task.weather.client;


import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import net.java.trial_task.weather.config.WeatherProperties;
import net.java.trial_task.weather.exceptions.WeatherDataUnavailableException;

@Component
public class WeatherApiClient {

  private final RestClient restClient;
  private final WeatherProperties properties;

  public WeatherApiClient(RestClient weatherRestClient, WeatherProperties properties) {
    this.restClient = weatherRestClient;
    this.properties = properties;
  }

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
