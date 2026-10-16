package net.java.trial_task.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StationDto(
  @JacksonXmlProperty(localName = "name") String name,
  @JacksonXmlProperty(localName = "wmocode") String wmoCode,
  @JacksonXmlProperty(localName = "phenomenon") String phenomenon,
  @JacksonXmlProperty(localName = "airtemperature") Double airTemperature,
  @JacksonXmlProperty(localName = "windspeed") Double windSpeed) {
}