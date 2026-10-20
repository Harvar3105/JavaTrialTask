package net.java.trial_task.api.controller.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.java.trial_task.shared.service.WeatherService;

/**
 * REST controller for triggering weather data updates.
 *
 * <p>Base path: {@code /api/weather}. The controller only starts the update through
 * {@link WeatherService}; it does not return imported data or the import result.
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherDataController {
  private final WeatherService service;

  /**
   * Creates the controller.
   *
   * @param service service performing the weather update, must not be {@code null}
   */
  public WeatherDataController(
    @Autowired WeatherService service
  ) {
    this.service = service;
  }

  /**
   * Handles {@code POST /api/weather/updateWeatherData}.
   *
   * <p>Requests an update of the stored weather observations by calling
   * {@link WeatherService#updateWeatherAsync()} and responds with status 202 and no body.
   * Status 202 means that the request was accepted, not that the import has completed or
   * succeeded. Failures of the import are not reported to the caller through this response.
   * The request takes no parameters and no request body.
   */
  @PostMapping("/updateWeatherData")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void updateWeatherData(){
    service.updateWeatherAsync();
  }

}
