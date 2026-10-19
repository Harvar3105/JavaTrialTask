package net.java.trial_task.unit.controller;

import net.java.trial_task.api.controller.condition.ATEFController;
import net.java.trial_task.api.exceptionHandler.ATEFControllerAdvice;
import net.java.trial_task.application.domain.entity.condition.ATEF;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.condition.ATEFDataIntegrityViolationException;
import net.java.trial_task.application.exceptions.condition.ATEFNotFoundException;
import net.java.trial_task.application.service.condition.ATEFService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ATEFController.class)
@Import(ATEFControllerAdvice.class)
class ATEFControllerTests {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private ATEFService atefService;

  private ATEF atef;
  private final String BASE = "/api/atef";

  @BeforeEach
  void setUp() {
    atef = mock(ATEF.class);

    when(atef.getId()).thenReturn(1L);
    when(atef.getCondition()).thenReturn(Condition.inRange);
    when(atef.getTemperature()).thenReturn(new BigDecimal("10.0"));
    when(atef.getMaxTemperature()).thenReturn(new BigDecimal("20.0"));
    when(atef.getFee()).thenReturn(new BigDecimal("5.0"));
    when(atef.checkIsForbidden()).thenReturn(false);
    when(atef.getVehicles()).thenReturn(Set.of());

    when(atef.getCreatedAt())
      .thenReturn(Instant.parse("2026-01-01T10:00:00Z"));
    when(atef.getUpdatedAt())
      .thenReturn(Instant.parse("2026-01-02T10:00:00Z"));
  }

  @Test
  void getAll_shouldReturn200AndATEFs() throws Exception {
    when(atefService.getAll()).thenReturn(List.of(atef));

    mockMvc.perform(get(BASE))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].condition").value("inRange"))
      .andExpect(jsonPath("$[0].temperature").value(10.0))
      .andExpect(jsonPath("$[0].maxTemperature").value(20.0))
      .andExpect(jsonPath("$[0].forbidden").value(false))
      .andExpect(jsonPath("$[0].fee").value(5.0))
      .andExpect(jsonPath("$[0].vehicleIds").isArray())
      .andExpect(jsonPath("$[0].createdAt")
        .value("2026-01-01T10:00:00Z"))
      .andExpect(jsonPath("$[0].updatedAt")
        .value("2026-01-02T10:00:00Z"));

    verify(atefService).getAll();
  }

  @Test
  void getById_shouldReturn200AndATEF() throws Exception {
    when(atefService.getById(1L)).thenReturn(atef);

    mockMvc.perform(get(BASE + "/{id}", 1L))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.condition").value("inRange"))
      .andExpect(jsonPath("$.temperature").value(10.0))
      .andExpect(jsonPath("$.forbidden").value(false))
      .andExpect(jsonPath("$.fee").value(5.0));

    verify(atefService).getById(1L);
  }

  @Test
  void getById_shouldReturn404WhenNotFound() throws Exception {
    when(atefService.getById(99L))
      .thenThrow(new ATEFNotFoundException(99L));

    mockMvc.perform(get(BASE + "/{id}", 99L))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail")
        .value("ATEF not found: id=99"));
  }

  @Test
  void create_shouldReturn201AndLocation() throws Exception {
    when(atefService.create(
      eq(Condition.inRange),
      eq(new BigDecimal("10.0")),
      eq(new BigDecimal("20.0")),
      eq(new BigDecimal("5.0")),
      eq(Set.of(1L))
    )).thenReturn(atef);

    String request = """
      {
        "condition": "inRange",
        "temperature": 10.0,
        "maxTemperature": 20.0,
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": [1]
      }
      """;

    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isCreated())
      .andExpect(header().string(
        "Location",
        "http://localhost/api/atef/1"
      ))
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.condition").value("inRange"))
      .andExpect(jsonPath("$.fee").value(5.0))
      .andExpect(jsonPath("$.forbidden").value(false));

    verify(atefService).create(
      eq(Condition.inRange),
      eq(new BigDecimal("10.0")),
      eq(new BigDecimal("20.0")),
      eq(new BigDecimal("5.0")),
      eq(Set.of(1L))
    );
  }

  @Test
  void create_shouldResolveForbiddenFeeToNull() throws Exception {
    ATEF forbiddenATEF = mock(ATEF.class);

    when(forbiddenATEF.getId()).thenReturn(2L);
    when(forbiddenATEF.getCondition()).thenReturn(Condition.equal);
    when(forbiddenATEF.getTemperature())
      .thenReturn(new BigDecimal("10.0"));
    when(forbiddenATEF.getMaxTemperature()).thenReturn(null);
    when(forbiddenATEF.getFee()).thenReturn(null);
    when(forbiddenATEF.checkIsForbidden()).thenReturn(true);
    when(forbiddenATEF.getVehicles()).thenReturn(Set.of());
    when(forbiddenATEF.getCreatedAt())
      .thenReturn(Instant.parse("2026-01-01T10:00:00Z"));
    when(forbiddenATEF.getUpdatedAt())
      .thenReturn(Instant.parse("2026-01-01T10:00:00Z"));

    when(atefService.create(
      eq(Condition.equal),
      eq(new BigDecimal("10.0")),
      isNull(),
      isNull(),
      isNull()
    )).thenReturn(forbiddenATEF);

    String request = """
      {
        "condition": "equal",
        "temperature": 10.0,
        "maxTemperature": null,
        "forbidden": true,
        "fee": null,
        "vehicleIds": null
      }
      """;

    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.forbidden").value(true))
      .andExpect(jsonPath("$.fee").doesNotExist());

    verify(atefService).create(
      eq(Condition.equal),
      eq(new BigDecimal("10.0")),
      isNull(),
      isNull(),
      isNull()
    );
  }

  @Test
  void create_shouldReturn400WhenRequestIsInvalid() throws Exception {
    String request = """
      {
        "condition": "inRange",
        "temperature": 10.0,
        "maxTemperature": 20.0,
        "forbidden": false,
        "fee": null,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(atefService);
  }

  @Test
  void create_shouldReturn400WhenTemperatureExceedsDigitsLimit() throws Exception {
    String request = """
      {
        "condition": "equal",
        "temperature": 12345.0,
        "maxTemperature": null,
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(atefService);
  }

  @Test
  void create_shouldReturn400WhenConditionIsInvalidEnum() throws Exception {
    String request = """
      {
        "condition": "unknown",
        "temperature": 10.0,
        "maxTemperature": null,
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(atefService);
  }

  @Test
  void create_shouldReturn400WhenServiceRejectsData() throws Exception {
    when(atefService.create(
      any(),
      any(),
      any(),
      any(),
      any()
    )).thenThrow(
      new ATEFDataIntegrityViolationException(
        "Range must be specified!"
      )
    );

    String request = """
      {
        "condition": "inRange",
        "temperature": 10.0,
        "maxTemperature": 20.0,
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.detail")
        .value("Range must be specified!"));
  }

  @Test
  void update_shouldReturn200AndUpdatedATEF() throws Exception {
    when(atefService.update(
      eq(1L),
      eq(Condition.inRange),
      eq(new BigDecimal("12.0")),
      eq(new BigDecimal("25.0")),
      eq(new BigDecimal("7.0"))
    )).thenReturn(atef);

    String request = """
      {
        "condition": "inRange",
        "temperature": 12.0,
        "maxTemperature": 25.0,
        "forbidden": false,
        "fee": 7.0
      }
      """;

    mockMvc.perform(put(BASE + "/{id}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.condition").value("inRange"));

    verify(atefService).update(
      1L,
      Condition.inRange,
      new BigDecimal("12.0"),
      new BigDecimal("25.0"),
      new BigDecimal("7.0")
    );
  }

  @Test
  void update_shouldReturn404WhenATEFDoesNotExist() throws Exception {
    when(atefService.update(
      eq(99L),
      any(),
      any(),
      any(),
      any()
    )).thenThrow(new ATEFNotFoundException(99L));

    String request = """
      {
        "condition": "inRange",
        "temperature": 12.0,
        "maxTemperature": 25.0,
        "forbidden": false,
        "fee": 7.0
      }
      """;

    mockMvc.perform(put(BASE + "/{id}", 99L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail")
        .value("ATEF not found: id=99"));
  }

  @Test
  void update_shouldReturn400WhenFeeIsInconsistent() throws Exception {
    String request = """
      {
        "condition": "equal",
        "temperature": 10.0,
        "maxTemperature": null,
        "forbidden": true,
        "fee": 5.0
      }
      """;

    mockMvc.perform(put(BASE + "/{id}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(atefService);
  }

  @Test
  void delete_shouldReturn204() throws Exception {
    doNothing().when(atefService).delete(1L);

    mockMvc.perform(delete(BASE + "/{id}", 1L))
      .andExpect(status().isNoContent())
      .andExpect(content().string(""));

    verify(atefService).delete(1L);
  }

  @Test
  void delete_shouldReturn404WhenATEFDoesNotExist()
    throws Exception {

    doThrow(new ATEFNotFoundException(99L))
      .when(atefService).delete(99L);

    mockMvc.perform(delete(BASE + "/{id}", 99L))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail")
        .value("ATEF not found: id=99"));
  }
}
