package net.java.trial_task.application.domain.entity.data;

import org.springframework.dao.DataIntegrityViolationException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import net.java.trial_task.shared.domain.entity.AbstractEntity;

@Entity 
@Table (name = "city")
public class City extends AbstractEntity {
  @NotBlank 
  @Column(nullable = false, unique = true)
  private String cityName;

  protected City() {}

  public City(String name){
    this.cityName = name;
  }

  public String getCityName(){
    return this.cityName;
  }

  public void setCityName(String cityName) {
    if (cityName == null || cityName.isBlank()) throw new DataIntegrityViolationException("Invalid data provided: " + cityName);
    this.cityName = cityName;
  }
}
