package net.java.trial_task.api.contracts.condition.wsef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;
import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;

@Schema(description = "Wind speed extra fee (WSEF) rule")
public record WSEFResponse(
  @Schema(description = "Unique identifier, assigned by the database", example = "1")
  Long id,

  @Schema(description = "Comparison operator applied to the observed wind speed")
  Condition condition,

  @Schema(description = "Threshold wind speed in m/s; lower bound for range operators", example = "10")
  BigDecimal windSpeed,

  @Schema(
    description = "Upper bound wind speed in m/s; used only by range operators, otherwise null",
    example = "20",
    nullable = true
  )
  BigDecimal maxWindSpeed,

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
  public static WSEFResponse from(WSEF atef) {
    return new WSEFResponse(
      atef.getId(),
      atef.getCondition(),
      atef.getWindSpeed(),
      atef.getMaxWindSpeed(),
      atef.checkIsForbidden(),
      atef.getFee(),
      atef.getVehicles().stream().map(Vehicle::getId).collect(Collectors.toSet()),
      atef.getCreatedAt(),
      atef.getUpdatedAt()
    );
  }
}
