package net.java.trial_task.unit.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.condition.WPEFDataIntegrityViolationException;

public class WPEFTests {

  private static final BigDecimal FEE = new BigDecimal("1.0");

  private static WPEF create(Condition c, String phenomenon) {
    return new WPEF(c, phenomenon, FEE, new HashSet<>());
  }

  private static WPEF valid() {
    return create(Condition.related, "snow");
  }

  // ---------- validateData (public) ----------

  @Test
  void validateData_nullCondition_throws() {
    var ex = assertThrows(WPEFDataIntegrityViolationException.class,
        () -> valid().validateData(null, "snow"));
    assertEquals("Only 'related' condition is allowed!", ex.getMessage());
  }

  @ParameterizedTest
  @EnumSource(value = Condition.class, names = "related", mode = EnumSource.Mode.EXCLUDE)
  void validateData_nonRelatedCondition_throws(Condition c) {
    var ex = assertThrows(WPEFDataIntegrityViolationException.class,
        () -> valid().validateData(c, "snow"));
    assertEquals("Only 'related' condition is allowed!", ex.getMessage());
  }

  @Test
  void validateData_nullPhenomenon_throws() {
    var ex = assertThrows(WPEFDataIntegrityViolationException.class,
        () -> valid().validateData(Condition.related, null));
    assertEquals("Phenomenon cannot be blank", ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "   ", "\t", "\n", " \t\n "})
  void validateData_blankPhenomenon_throws(String phenomenon) {
    assertThrows(WPEFDataIntegrityViolationException.class,
        () -> valid().validateData(Condition.related, phenomenon));
  }

  @Test
  void validateData_nullConditionAndNullPhenomenon_reportsConditionFirst() {
    var ex = assertThrows(WPEFDataIntegrityViolationException.class,
        () -> valid().validateData(null, null));
    assertEquals("Only 'related' condition is allowed!", ex.getMessage());
  }

  @Test
  void validateData_validData_doesNotThrow() {
    assertDoesNotThrow(() -> valid().validateData(Condition.related, "snow"));
  }

  // ---------- constructor ----------

  @Test
  void constructor_nonRelatedCondition_throws() {
    assertThrows(WPEFDataIntegrityViolationException.class,
        () -> create(Condition.inRange, "snow"));
  }

  @Test
  void constructor_blankPhenomenon_throws() {
    assertThrows(WPEFDataIntegrityViolationException.class,
        () -> create(Condition.related, " "));
  }

  @Test
  void constructor_validData_createsEntity() {
    WPEF wpef = create(Condition.related, "snow");
    assertEquals(Condition.related, wpef.getCondition());
    assertEquals("snow", wpef.getPhenomenon());
    assertEquals(FEE, wpef.getFee());
  }

  @Test
  void constructor_nullFee_isAllowed() {
    assertDoesNotThrow(() -> new WPEF(Condition.related, "glaze", null, new HashSet<>()));
  }

  // ---------- update ----------

  @Test
  void update_validData_changesFields() {
    WPEF wpef = valid();
    wpef.update(Condition.related, "rain", new BigDecimal("0.5"));

    assertEquals("rain", wpef.getPhenomenon());
    assertEquals(new BigDecimal("0.5"), wpef.getFee());
  }

  @Test
  void update_invalidCondition_throwsAndKeepsState() {
    WPEF wpef = valid();
    assertThrows(WPEFDataIntegrityViolationException.class,
        () -> wpef.update(Condition.inRange, "rain", BigDecimal.ZERO));
    assertUnchanged(wpef);
  }

  @Test
  void update_nullCondition_throwsAndKeepsState() {
    WPEF wpef = valid();
    assertThrows(WPEFDataIntegrityViolationException.class,
        () -> wpef.update(null, "rain", BigDecimal.ZERO));
    assertUnchanged(wpef);
  }

  @Test
  void update_blankPhenomenon_throwsAndKeepsState() {
    WPEF wpef = valid();
    assertThrows(WPEFDataIntegrityViolationException.class,
        () -> wpef.update(Condition.related, "  ", BigDecimal.ZERO));
    assertUnchanged(wpef);
  }

  private static void assertUnchanged(WPEF wpef) {
    assertEquals(Condition.related, wpef.getCondition());
    assertEquals("snow", wpef.getPhenomenon());
    assertEquals(FEE, wpef.getFee());
  }
}
