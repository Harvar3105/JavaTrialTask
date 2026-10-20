package net.java.trial_task.application.domain.entity.data;

import org.springframework.dao.DataIntegrityViolationException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import net.java.trial_task.shared.domain.entity.AbstractEntity;

@Entity 
@Table (name = "vehicle")
public class Vehicle extends AbstractEntity {
  @NotBlank 
  @Column(nullable = false, unique = true)
  private String vehicleType;

  protected Vehicle() {}

  public Vehicle(String type) {
    this.vehicleType = type;
  }

  public String getVehicleType() {
    return vehicleType;
  }

  public void setVehicleType(String type) {
    if (type == null || type.isBlank()) throw new DataIntegrityViolationException("Invalid data provided: " + type);
    this.vehicleType = type;
  }
}
