package net.java.trial_task.application.util;

import java.math.BigDecimal;

import net.java.trial_task.application.domain.enums.Condition;

public final class ConditionEvaluator {

  private ConditionEvaluator() {}

  /**
   * Checks whether a numeric value satisfies a rule condition.
   * Range conditions are inclusive on both bounds.
   */
  public static boolean matches(Condition condition, BigDecimal value, BigDecimal bound, BigDecimal maxBound) {
    int cmp = value.compareTo(bound);
    return switch (condition) {
      case less -> cmp < 0;
      case greater -> cmp > 0;
      case equal -> cmp == 0;
      case lessOrEqual -> cmp <= 0;
      case greaterOrEqual -> cmp >= 0;
      case inRange -> isInRange(value, bound, maxBound);
      case notInRange -> !isInRange(value, bound, maxBound);
      case related -> throw new IllegalArgumentException("Condition 'related' is not applicable to numeric values");
    };
  }

  private static boolean isInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
    return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
  }
}
