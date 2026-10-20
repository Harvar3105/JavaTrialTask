package net.java.trial_task.application.domain.entity.condition;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import net.java.trial_task.shared.domain.entity.AbstractEntity;

@Entity 
@Table (name = "rbf")
public class RBF extends AbstractEntity {
  @Column (nullable = false, updatable = false)
  private Long cityId;
  @Column (nullable = false, updatable = false)
  private Long vehicleTypeId;
  @Column (nullable = true, precision = 5, scale = 1)
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

  public BigDecimal getFee() {
    return fee;
  }

  public void setFee(BigDecimal newFee) {
    this.fee = newFee;
  }

}
