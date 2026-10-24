package net.java.trial_task.application.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
  Comparison operator applied to the observed value. 'inRange' and 'notInRange' use both \
  the base value and the upper bound field; all other operators use only the base value. \
  'related' matches by keyword and is supported only for phenomenon rules.""")
public enum Condition {
  less,
  greater,
  equal,
  lessOrEqual,
  greaterOrEqual,
  inRange,
  notInRange,
  related
}
