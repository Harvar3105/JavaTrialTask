package net.java.trial_task.shared.domain.entity.condition;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.java.trial_task.shared.domain.entity.AbstractEntity;

@Entity 
@Table (name = "rbf")
public class RBF extends AbstractEntity {
  private Long cityId;
  private Long vehicleTypeId;
  private BigDecimal fee;

  public RBF() {}

  public RBF(Long city, Long vehicleType, BigDecimal fee) {
    this.cityId = city;
    this.vehicleTypeId = vehicleType;
    this.fee = fee;
  }

  public Long getCityId() {
    return cityId;
  }

  public Long getVehicleTypeId() {
    return vehicleTypeId;
  }

  public BigDecimal getBaseFee() {
    return fee;
  }
  
}
