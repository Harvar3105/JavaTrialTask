package net.java.trial_task.shared.domain.entity.condition;

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
import net.java.trial_task.shared.domain.entity.AbstractEntity;
import net.java.trial_task.shared.domain.entity.data.Vehicle;
import net.java.trial_task.shared.domain.enums.Condition;

@Entity 
@Table (name = "atef")
public class ATEF extends AbstractEntity {
  @Column (nullable = false)
  @Enumerated (EnumType.STRING)
  private Condition condition;

  @Column (nullable = false, precision = 5, scale = 1)
  private BigDecimal temperature;

  @Column (nullable = false, precision = 5, scale = 1)
  private BigDecimal maxTemperature;

  //No fee = 0, forbidden = null
  @Column (nullable = false, precision = 5, scale = 1)
  private BigDecimal fee;

  @ManyToMany
  @JoinTable(
    name = "atef_relation",
    joinColumns = @JoinColumn(name = "atef_id"),
    inverseJoinColumns = @JoinColumn(name = "vehicle_id")
  )
  private Set<Vehicle> vehicles = new HashSet<>();

  protected ATEF(){}

  public ATEF(
    Condition condition,
    BigDecimal temperature,
    BigDecimal maxTemperature,
    BigDecimal fee,
    Set<Vehicle> vehicles
  ){
    this.condition = condition;
    this.temperature = temperature;
    this.maxTemperature = maxTemperature;
    this.fee = fee;
    this.vehicles = new HashSet<>(vehicles);
  }

  public Condition getCondition(){
    return this.condition;
  }

  public BigDecimal getTemperature(){
    return this.temperature;
  }

  public BigDecimal getMaxTemperature(){
    return this.maxTemperature;
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
