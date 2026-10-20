package net.java.trial_task.unit.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.condition.ATEFDataIntegrityViolationException;

public class ATEFTests {

  private static final BigDecimal TEMP = new BigDecimal("-10.0");
  private static final BigDecimal MAX = new BigDecimal("0.0");
  private static final BigDecimal FEE = new BigDecimal("0.5");

  private static ATEF create(Condition c, BigDecimal temp, BigDecimal max) {
    return new ATEF(c, temp, max, FEE, null);
  }

  private static ATEF validRange() {
    return create(Condition.inRange, TEMP, MAX);
  }

  // ---------- constructor ----------

  @Test
  void constructor_nullCondition_throws() {
    var ex = assertThrows(ATEFDataIntegrityViolationException.class,
      () -> create(null, TEMP, null));
    assertEquals("Condition must be provided!", ex.getMessage());
  }

  @Test
  void constructor_nullTemperature_throws() {
    var ex = assertThrows(ATEFDataIntegrityViolationException.class,
      () -> create(Condition.inRange, null, MAX));
    assertEquals("Temperature must be provided!", ex.getMessage());
  }

  @Test
  void constructor_nullConditionAndNullTemperature_reportsConditionFirst() {
    var ex = assertThrows(ATEFDataIntegrityViolationException.class,
      () -> create(null, null, null));
    assertEquals("Condition must be provided!", ex.getMessage());
  }

  @Test
  void constructor_relatedCondition_throws() {
    assertThrows(ATEFDataIntegrityViolationException.class,
      () -> create(Condition.related, TEMP, null));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_rangeConditionWithoutMax_throws(Condition c) {
    var ex = assertThrows(ATEFDataIntegrityViolationException.class,
      () -> create(c, TEMP, null));
    assertEquals("Range must be specified!", ex.getMessage());
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"less", "greater", "equal", "lessOrEqual", "greaterOrEqual"})
  void constructor_nonRangeConditionWithMax_throws(Condition c) {
    var ex = assertThrows(ATEFDataIntegrityViolationException.class,
      () -> create(c, TEMP, MAX));
    assertEquals("Max temperature must be provided only for range conditions!", ex.getMessage());
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_temperatureEqualToMax_throws(Condition c) {
    assertThrows(ATEFDataIntegrityViolationException.class,
      () -> create(c, MAX, MAX));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_temperatureEqualToMaxWithDifferentScale_throws(Condition c) {
    assertThrows(ATEFDataIntegrityViolationException.class,
      () -> create(c, new BigDecimal("0.00"), new BigDecimal("0.0")));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_temperatureGreaterThanMax_throws(Condition c) {
    assertThrows(ATEFDataIntegrityViolationException.class,
      () -> create(c, new BigDecimal("0.1"), MAX));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"inRange", "notInRange"})
  void constructor_validRange_createsEntity(Condition c) {
    ATEF atef = create(c, TEMP, MAX);
    assertEquals(c, atef.getCondition());
    assertEquals(TEMP, atef.getTemperature());
    assertEquals(MAX, atef.getMaxTemperature());
  }

  @Test
  void constructor_temperatureJustBelowMax_createsEntity() {
    assertDoesNotThrow(() -> create(Condition.inRange, new BigDecimal("-0.1"), MAX));
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = {"less", "greater", "equal", "lessOrEqual", "greaterOrEqual"})
  void constructor_validNonRange_createsEntity(Condition c) {
    ATEF atef = create(c, TEMP, null);
    assertEquals(c, atef.getCondition());
    assertNull(atef.getMaxTemperature());
  }

  @Test
  void constructor_nullFee_isAllowed() {
    assertDoesNotThrow(() -> new ATEF(Condition.inRange, TEMP, MAX, null, null));
  }

  // ---------- update ----------

  @Test
  void update_validData_changesFields() {
    ATEF atef = validRange();
    atef.update(Condition.notInRange, new BigDecimal("-30.0"), new BigDecimal("-20.0"), BigDecimal.ONE);

    assertEquals(Condition.notInRange, atef.getCondition());
    assertEquals(new BigDecimal("-30.0"), atef.getTemperature());
    assertEquals(new BigDecimal("-20.0"), atef.getMaxTemperature());
    assertEquals(BigDecimal.ONE, atef.getFee());
  }

  @Test
  void update_nullCondition_throwsAndKeepsState() {
    ATEF atef = validRange();
    assertThrows(ATEFDataIntegrityViolationException.class,
      () -> atef.update(null, TEMP, MAX, FEE));
    assertUnchanged(atef);
  }

  @Test
  void update_nullTemperature_throwsAndKeepsState() {
    ATEF atef = validRange();
    assertThrows(ATEFDataIntegrityViolationException.class,
      () -> atef.update(Condition.inRange, null, MAX, FEE));
    assertUnchanged(atef);
  }

  @Test
  void update_related_throwsAndKeepsState() {
    ATEF atef = validRange();
    assertThrows(ATEFDataIntegrityViolationException.class,
      () -> atef.update(Condition.related, TEMP, null, FEE));
    assertUnchanged(atef);
  }

  @Test
  void update_rangeWithoutMax_throwsAndKeepsState() {
    ATEF atef = validRange();
    assertThrows(ATEFDataIntegrityViolationException.class,
      () -> atef.update(Condition.inRange, TEMP, null, FEE));
    assertUnchanged(atef);
  }

  @Test
  void update_temperatureNotLessThanMax_throwsAndKeepsState() {
    ATEF atef = validRange();
    assertThrows(ATEFDataIntegrityViolationException.class,
      () -> atef.update(Condition.inRange, MAX, MAX, FEE));
    assertUnchanged(atef);
  }

  private static void assertUnchanged(ATEF atef) {
    assertEquals(Condition.inRange, atef.getCondition());
    assertEquals(TEMP, atef.getTemperature());
    assertEquals(MAX, atef.getMaxTemperature());
    assertEquals(FEE, atef.getFee());
  }
} 
