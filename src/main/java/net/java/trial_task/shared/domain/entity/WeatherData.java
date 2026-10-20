package net.java.trial_task.shared.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity 
@Table (name = "weather_data")
public class WeatherData extends AbstractEntity {

  @Column(nullable = false)
  private String station;

  @Column(nullable = true)
  private Integer wmoCode;

  @Column(nullable = true, precision = 5, scale = 1)
  private BigDecimal airTemperature;

  @Column(nullable = true, precision = 5, scale = 1)
  private BigDecimal windSpeed;

  @Column(nullable = true)
  private String phenomenon;

  @Column(nullable = false)
  private Instant observationTime;

  protected WeatherData(){
  }

  public WeatherData(
    String station,
    Integer WMOCode,
    BigDecimal airTemperature,
    BigDecimal windSpeed,
    String phenomenon,
    Instant observationTime
  ) {
    this.station = station;
    this.wmoCode = WMOCode;
    this.airTemperature = airTemperature;
    this.windSpeed = windSpeed;
    this.phenomenon = phenomenon;
    this.observationTime = observationTime;
  }

  public String getStation() {
    return station;
  }

  public Integer getWmoCode() {
    return wmoCode;
  }

  public BigDecimal getAirTemperature() {
    return airTemperature;
  }

  public BigDecimal getWindSpeed() {
    return windSpeed;
  }

  public String getPhenomenon() {
    return phenomenon;
  }

  public Instant getObservationTime() {
    return observationTime;
  }
}
