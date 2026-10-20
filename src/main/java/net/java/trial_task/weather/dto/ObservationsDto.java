package net.java.trial_task.weather.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ObservationsDto(
  @JacksonXmlProperty(isAttribute = true, localName = "timestamp")
  Long timestamp,

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "station")
  List<StationDto> stations) {
}