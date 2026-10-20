package net.java.trial_task.unit.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.condition.WSEFDataIntegrityViolationException;

public class WSEFTests {

  private static final BigDecimal SPEED = new BigDecimal("10.0");
  private static final BigDecimal MAX = new BigDecimal("20.0");
  private static final BigDecimal FEE = new BigDecimal("0.5");

  private static WSEF create(Condition c, BigDecimal speed, BigDecimal max) {
    return new WSEF(c, speed, max, FEE, null);
  }

  private static WSEF validRange() {
    return create(Condition.inRange, SPEED, MAX);
  }

  // ---------- constructor ----------

  @Test
  void constructor_nullCondition_throws() {
    var ex = assertThrows(WSEFDataIntegrityViolationException.class,
      () -> create(null, SPEED, null));
    assertEquals("Condition must be provided!", ex.getMessage());
  }

  @Test
  void constructor_nullWindSpeed_throws() {
    var ex = assertThrows(WSEFDataIntegrityViolationException.class,
      () -> create(Condition.inRange, null, MAX));
    assertEquals("Wind speed must be provided!", ex.getMessage());
  }

  @Test
  void constructor_nullConditionAndNullWindSpeed_reportsConditionFirst() {
    var ex = assertThrows(WSEFDataIntegrityViolationException.class,
      () -> create(null, null, null));
    assertEquals("Condition must be provided!", ex.getMessage());
  }

  @Test
  void constructor_relatedCondition_throws() {
    assertThrows(WSEFDataIntegrityViolationException.class,
        () -> create(Condition.related, SPEED, null));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_rangeConditionWithoutMax_throws(Condition c) {
    var ex = assertThrows(WSEFDataIntegrityViolationException.class,
        () -> create(c, SPEED, null));
    assertEquals("Range must be specified!", ex.getMessage());
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"less", "greater", "equal", "lessOrEqual", "greaterOrEqual"})
  void constructor_nonRangeConditionWithMax_throws(Condition c) {
    var ex = assertThrows(WSEFDataIntegrityViolationException.class,
        () -> create(c, SPEED, MAX));
    assertEquals("Max wind speed must be provided only for range conditions!", ex.getMessage());
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_windSpeedEqualToMax_throws(Condition c) {
    assertThrows(WSEFDataIntegrityViolationException.class,
        () -> create(c, MAX, MAX));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_windSpeedEqualToMaxWithDifferentScale_throws(Condition c) {
    // compareTo ignores scale: 20.0 == 20.00
    assertThrows(WSEFDataIntegrityViolationException.class,
        () -> create(c, new BigDecimal("20.00"), new BigDecimal("20.0")));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_windSpeedGreaterThanMax_throws(Condition c) {
    assertThrows(WSEFDataIntegrityViolationException.class,
        () -> create(c, new BigDecimal("20.1"), MAX));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_validRange_createsEntity(Condition c) {
    WSEF wsef = create(c, SPEED, MAX);
    assertEquals(c, wsef.getCondition());
    assertEquals(SPEED, wsef.getWindSpeed());
    assertEquals(MAX, wsef.getMaxWindSpeed());
  }

  @Test
  void constructor_windSpeedJustBelowMax_createsEntity() {
    assertDoesNotThrow(() -> create(Condition.inRange, new BigDecimal("19.9"), MAX));
  }

  @Test
  void constructor_negativeWindSpeedBelowMax_createsEntity() {
    assertDoesNotThrow(() -> create(Condition.inRange, new BigDecimal("-5.0"), MAX));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"less", "greater", "equal", "lessOrEqual", "greaterOrEqual"})
  void constructor_validNonRange_createsEntity(Condition c) {
    WSEF wsef = create(c, SPEED, null);
    assertEquals(c, wsef.getCondition());
    assertNull(wsef.getMaxWindSpeed());
  }

  @Test
  void constructor_nullFee_isAllowed() {
    assertDoesNotThrow(() -> new WSEF(Condition.inRange, SPEED, MAX, null, null));
  }

  // ---------- update ----------

  @Test
  void update_validData_changesFields() {
    WSEF wsef = validRange();
    wsef.update(Condition.notInRange, new BigDecimal("1.0"), new BigDecimal("5.0"), BigDecimal.ZERO);

    assertEquals(Condition.notInRange, wsef.getCondition());
    assertEquals(new BigDecimal("1.0"), wsef.getWindSpeed());
    assertEquals(new BigDecimal("5.0"), wsef.getMaxWindSpeed());
    assertEquals(BigDecimal.ZERO, wsef.getFee());
  }

  @Test
  void update_nullCondition_throwsAndKeepsState() {
    WSEF wsef = validRange();
    assertThrows(WSEFDataIntegrityViolationException.class,
        () -> wsef.update(null, SPEED, MAX, FEE));
    assertUnchanged(wsef);
  }

  @Test
  void update_nullWindSpeed_throwsAndKeepsState() {
    WSEF wsef = validRange();
    assertThrows(WSEFDataIntegrityViolationException.class,
        () -> wsef.update(Condition.inRange, null, MAX, FEE));
    assertUnchanged(wsef);
  }

  @Test
  void update_related_throwsAndKeepsState() {
    WSEF wsef = validRange();
    assertThrows(WSEFDataIntegrityViolationException.class,
        () -> wsef.update(Condition.related, SPEED, null, FEE));
    assertUnchanged(wsef);
  }

  @Test
  void update_rangeWithoutMax_throwsAndKeepsState() {
    WSEF wsef = validRange();
    assertThrows(WSEFDataIntegrityViolationException.class,
        () -> wsef.update(Condition.inRange, SPEED, null, FEE));
    assertUnchanged(wsef);
  }

  @Test
  void update_windSpeedNotLessThanMax_throwsAndKeepsState() {
    WSEF wsef = validRange();
    assertThrows(WSEFDataIntegrityViolationException.class,
        () -> wsef.update(Condition.inRange, MAX, MAX, FEE));
    assertUnchanged(wsef);
  }

  private static void assertUnchanged(WSEF wsef) {
    assertEquals(Condition.inRange, wsef.getCondition());
    assertEquals(SPEED, wsef.getWindSpeed());
    assertEquals(MAX, wsef.getMaxWindSpeed());
    assertEquals(FEE, wsef.getFee());
  }
}