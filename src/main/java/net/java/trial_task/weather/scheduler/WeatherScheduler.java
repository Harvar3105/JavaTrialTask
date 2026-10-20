package net.java.trial_task.weather.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.java.trial_task.shared.service.WeatherService;

@Component
public class WeatherScheduler {

  private final WeatherService service;

  public WeatherScheduler(WeatherService weatherService) {
    this.service = weatherService;
  }

  @Scheduled(cron = "${weather.cron:0 15 * * * *}")
  public void updateWeather() {
    service.updateWeatherAsync();
  }
}
