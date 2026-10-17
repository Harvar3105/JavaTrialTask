package net.java.trial_task.application.domain.entity.condition;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.shared.domain.entity.AbstractEntity;

@Entity 
@Table (name = "wsef")
public class WSEF extends AbstractEntity{
  @Column (nullable = false)
  @Enumerated (EnumType.STRING)
  private Condition condition;

  @Column (nullable = false, precision = 5, scale = 1)
  private BigDecimal windSpeed;

  @Column (nullable = false, precision = 5, scale = 1)
  private BigDecimal maxWindSpeed;

  //No fee = 0, forbidden = null
  @Column (nullable = false, precision = 5, scale = 1)
  private BigDecimal fee;

  @ManyToMany
  @JoinTable(
    name = "wsef_relation",
    joinColumns = @JoinColumn(name = "wsef_id"),
    inverseJoinColumns = @JoinColumn(name = "vehicle_id")
  )
  private Set<Vehicle> vehicles = new HashSet<>();

  protected WSEF(){}
  public WSEF(
    Condition condition,
    BigDecimal windSpeed,
    BigDecimal maxWindSpeed,
    BigDecimal fee,
    Set<Vehicle> vehicles
  ) {
    this.condition = condition;
    this.windSpeed = windSpeed;
    this.maxWindSpeed = maxWindSpeed;
    this.fee = fee;
    this.vehicles = new HashSet<>(vehicles);
  }

  public Condition getCondition(){
    return this.condition;
  }

  public BigDecimal getWindSpeed(){
    return this.windSpeed;
  }

  public BigDecimal getMaxWindSpeed(){
    return this.maxWindSpeed;
  }

  public BigDecimal getFee(){
    return this.fee;
  }

  public boolean checkIsForbidden(){
    return this.fee == null;
  }

  public Set<Vehicle> getVehicles() {
    return Collections.unmodifiableSet(vehicles);
  }

  public void addVehicle(Vehicle vehicle) {
    vehicles.add(vehicle);
  }

  public void removeVehicle(Vehicle vehicle) {
    vehicles.remove(vehicle);
  }
  
}
