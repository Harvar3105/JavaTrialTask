package net.java.trial_task.weather.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.java.trial_task.shared.domain.entity.data.WeatherData;
import net.java.trial_task.shared.repository.WeatherRepository;
import net.java.trial_task.weather.client.WeatherApiClient;
import net.java.trial_task.weather.dto.ObservationsDto;
import net.java.trial_task.weather.dto.StationDto;

@Service 
public class WeatherService {
  private final ObservationsXmlParser parser;
  private final WeatherApiClient client;
  private final WeatherRepository repo;
  private final AtomicBoolean importRunning = new AtomicBoolean(false);
  private static final Logger log = LoggerFactory.getLogger(WeatherService.class);


  public WeatherService(
    @Autowired ObservationsXmlParser parser,
    @Autowired WeatherApiClient client,
    @Autowired WeatherRepository repo
  ){
    this.parser = parser;
    this.client =  client;
    this.repo = repo;
  }

  public void saveWeatherData(Collection<WeatherData> weather){
    if (weather.isEmpty()) return;
    repo.saveAllAndFlush(weather);
  }

  public ObservationsDto getWeatherDataDto(){
    var data = client.fetchObservationsXml();
    return parser.parse(data);
  }

  public Collection<WeatherData> parseDto(ObservationsDto dto){
    List<WeatherData> result = new ArrayList<>();
    var timestamp = Instant.ofEpochSecond(dto.timestamp());
    for (var station : dto.stations()){
      var parsed = parseStationDto(station, timestamp);
      if (parsed == null) continue;

      result.add(parsed);
    }
    return result;
  }

  private WeatherData parseStationDto(StationDto dto, Instant timestamp){
    if (dto.name() == null) return null;

    return new WeatherData(
      dto.name(),
      dto.wmoCode() != null ? Integer.parseInt(dto.wmoCode()) : null,
      dto.airTemperature() != null ? BigDecimal.valueOf(dto.airTemperature()) : null,
      dto.windSpeed() != null ? BigDecimal.valueOf(dto.windSpeed()) : null,
      dto.phenomenon(),
      timestamp
    );
  }

  public void updateWeather(){
    var dto = getWeatherDataDto();
    var data = parseDto(dto);
    saveWeatherData(data);
  }

  @Async
  public void updateWeatherAsync(){
    if (!importRunning.compareAndSet(false, true)) {
      log.warn("Weather import skipped: another import is already running");
      return;
    }
    try {
      log.info("Weather import started");
      var dto = getWeatherDataDto();
      var data = parseDto(dto);
      saveWeatherData(data);
      log.info("Weather import finished: {} entries saved", data.size());
    } catch (RuntimeException e) {
      log.error("Weather import failed", e);
    } finally {
      importRunning.set(false);
    }
  }

}
