package net.java.trial_task.api.contracts.condition.wpef;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;
import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.domain.enums.Condition;

@Schema(description = "Weather phenomenon extra fee (WPEF) rule")
public record WPEFResponse(
  @Schema(description = "Unique identifier, assigned by the database", example = "1")
  Long id,

  @Schema(description = "Operator applied to the observed weather phenomenon; 'related' matches by keyword")
  Condition condition,

  @Schema(description = "Phenomenon name or keyword the rule is matched against", example = "snow")
  String phenomenon,

  @Schema(description = "True if the rule forbids usage of the listed vehicle types (fee is null)")
  boolean forbidden,

  @Schema(
    description = "Extra fee in EUR; null when the rule forbids usage of the vehicle types",
    example = "1.0",
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
  public static WPEFResponse from(WPEF wpef) {
    return new WPEFResponse(
      wpef.getId(),
      wpef.getCondition(),
      wpef.getPhenomenon(),
      wpef.checkIsForbidden(),
      wpef.getFee(),
      wpef.getVehicles().stream().map(Vehicle::getId).collect(Collectors.toSet()),
      wpef.getCreatedAt(),
      wpef.getUpdatedAt()
    );
  }
}
