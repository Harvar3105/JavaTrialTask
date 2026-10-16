package net.java.trial_task.shared.domain.entity.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.java.trial_task.shared.domain.entity.AbstractEntity;

@Entity 
@Table (name = "vehicle")
public class Vehicle extends AbstractEntity {
  @Column(nullable = false)
  private String vehicleType;

  protected Vehicle() {}

  public Vehicle(String type) {
    this.vehicleType = type;
  }

  public String getVehicleType() {
    return vehicleType;
  }
}
