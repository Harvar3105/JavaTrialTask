package net.java.trial_task.api.contracts.condition.atef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;
import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;

@Schema(description = "Air temperature extra fee (ATEF) rule")
public record ATEFResponse(
  @Schema(description = "Unique identifier, assigned by the database", example = "1")
  Long id,

  @Schema(description = "Comparison operator applied to the observed air temperature")
  Condition condition,

  @Schema(description = "Threshold air temperature in °C; lower bound for range operators", example = "-10")
  BigDecimal temperature,

  @Schema(
    description = "Upper bound air temperature in °C; used only by range operators, otherwise null",
    example = "0",
    nullable = true
  )
  BigDecimal maxTemperature,

  @Schema(description = "True if the rule forbids usage of the listed vehicle types (fee is null)")
  boolean forbidden,

  @Schema(
    description = "Extra fee in EUR; null when the rule forbids usage of the vehicle types",
    example = "0.5",
    nullable = true
  )
  BigDecimal fee,

  @Schema(description = "Identifiers of vehicle types to which the rule applies")
  Set<Long> vehicleIds,

  @Schema(description = "Creation timestamp, UTC (ISO-8601)", example = "2026-10-24T12:00:00Z")
  Instant createdAt,

  @Schema(description = "Last modification timestamp, UTC (ISO-8601)", example = "2026-10-24T12:00:00Z")
  Instant updatedAt
) {
  public static ATEFResponse from(ATEF atef) {
    return new ATEFResponse(
      atef.getId(),
      atef.getCondition(),
      atef.getTemperature(),
      atef.getMaxTemperature(),
      atef.checkIsForbidden(),
      atef.getFee(),
      atef.getVehicles().stream().map(Vehicle::getId).collect(Collectors.toSet()),
      atef.getCreatedAt(),
      atef.getUpdatedAt()
    );
  }
}
