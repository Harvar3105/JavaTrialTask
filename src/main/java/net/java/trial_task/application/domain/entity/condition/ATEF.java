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
import net.java.trial_task.application.exceptions.ATEFDataIntegrityViolationException;
import net.java.trial_task.shared.domain.entity.AbstractEntity;

@Entity 
@Table (name = "atef")
public class ATEF extends AbstractEntity {
  @Column (nullable = false)
  @Enumerated (EnumType.STRING)
  private Condition condition;

  @Column (nullable = false, precision = 5, scale = 1)
  private BigDecimal temperature;

  @Column (nullable = true, precision = 5, scale = 1)
  private BigDecimal maxTemperature;

  //No fee = 0, forbidden = null
  @Column (nullable = true, precision = 5, scale = 1)
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
    validateData(condition, temperature, maxTemperature);

    this.condition = condition;
    this.maxTemperature = maxTemperature;
    this.temperature = temperature;
    this.fee = fee;
    if (vehicles != null) this.vehicles = new HashSet<>(vehicles);
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
    if (vehicle == null) throw new ATEFDataIntegrityViolationException("Cannot add null to vehicles!");
    this.vehicles.add(vehicle);
  }

  public void removeVehicle(Vehicle vehicle) {
    if (vehicle == null) throw new ATEFDataIntegrityViolationException("Cannot remove null from vehicles!");
    this.vehicles.remove(vehicle);
  }

  public void update(Condition condition, BigDecimal temperature, BigDecimal maxTemperature, BigDecimal fee) {
    validateData(condition, temperature, maxTemperature);
    
    this.condition = condition;
    this.maxTemperature = maxTemperature;
    this.temperature = temperature;
    this.fee = fee;
  }

  private void validateData(Condition condition, BigDecimal temperature, BigDecimal maxTemp){
    if (condition == null) throw new ATEFDataIntegrityViolationException("Condition must be provided!");
    if (temperature == null) throw new ATEFDataIntegrityViolationException("Temperature must be provided!");

    if (condition == Condition.related)
      throw new ATEFDataIntegrityViolationException("ATEF cannot contain condition " + condition.toString());

    if (maxTemp == null && (condition == Condition.inRange || condition == Condition.notInRange))
      throw new ATEFDataIntegrityViolationException("Range must be specified!");
    if (maxTemp != null && (condition != Condition.inRange && condition != Condition.notInRange))
      throw new ATEFDataIntegrityViolationException("Max temperature must be provided only for range conditions!");

    if (maxTemp != null && temperature.compareTo(maxTemp) >= 0)
      throw new ATEFDataIntegrityViolationException("Temperature cannot be higher or equal to max temperature!");
  }

}
