package net.java.trial_task.weather.service;


import org.springframework.stereotype.Component;

import net.java.trial_task.weather.dto.ObservationsDto;
import net.java.trial_task.weather.exceptions.WeatherParseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlReadFeature;


@Component
public class ObservationsXmlParser {
  private final XmlMapper xmlMapper = XmlMapper.builder()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(XmlReadFeature.EMPTY_ELEMENT_AS_NULL)
    .build();

  public ObservationsDto parse(byte[] xml) {
    try {
      return xmlMapper.readValue(xml, ObservationsDto.class);
    } catch (JacksonException e) {
      throw new WeatherParseException("Failed to parse observations XML", e);
    }
  }
}
