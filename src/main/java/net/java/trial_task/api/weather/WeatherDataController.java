package net.java.trial_task.api.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.java.trial_task.weather.service.WeatherService;

@RestController 
@RequestMapping("/api/weather")
public class WeatherDataController {
  private final WeatherService service;

  public WeatherDataController(
    @Autowired WeatherService service
  ) {
    this.service = service;
  }

  @PostMapping("/updateWeatherData")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void updateWeatherData(){
    service.updateWeatherAsync();
  }

}
