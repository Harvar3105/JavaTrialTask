package net.java.trial_task.application.domain.entity.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.java.trial_task.shared.domain.entity.AbstractEntity;

@Entity 
@Table (name = "city")
public class City extends AbstractEntity {
  @Column(nullable = false)
  private String cityName;

  protected City(){}
  public City(String name){
    this.cityName = name;
  }

  public String getCityName(){
    return this.cityName;
  }
}
