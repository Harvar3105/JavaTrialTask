package net.java.trial_task.weather.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;


@ConfigurationProperties(prefix = "weather")
public record WeatherProperties(
  URI sourceUri,
  @DefaultValue("5s") Duration connectTimeout,
  @DefaultValue("10s") Duration readTimeout,
  String cron
) {
  
}
