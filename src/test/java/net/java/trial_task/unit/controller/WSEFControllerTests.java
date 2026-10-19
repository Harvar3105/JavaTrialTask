package net.java.trial_task.unit.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import net.java.trial_task.api.exceptionHandler.WSEFExceptionHandler;
import net.java.trial_task.api.wsef.WSEFController;
import net.java.trial_task.application.domain.entity.condition.WSEF;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.WSEFDataIntegrityViolationException;
import net.java.trial_task.application.exceptions.WSEFNotFoundException;
import net.java.trial_task.application.service.WSEFService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WSEFController.class)
@Import(WSEFExceptionHandler.class)
class WSEFControllerTests {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private WSEFService wsefService;

  private WSEF wsef;

  @BeforeEach
  void setUp() {
    wsef = mock(WSEF.class);

    when(wsef.getId()).thenReturn(1L);
    when(wsef.getCondition()).thenReturn(Condition.inRange);
    when(wsef.getWindSpeed()).thenReturn(new BigDecimal("10.0"));
    when(wsef.getMaxWindSpeed()).thenReturn(new BigDecimal("20.0"));
    when(wsef.getFee()).thenReturn(new BigDecimal("5.0"));
    when(wsef.checkIsForbidden()).thenReturn(false);
    when(wsef.getVehicles()).thenReturn(Set.of());
    when(wsef.getCreatedAt())
      .thenReturn(Instant.parse("2026-01-01T10:00:00Z"));
    when(wsef.getUpdatedAt())
      .thenReturn(Instant.parse("2026-01-02T10:00:00Z"));
  }

  @Test
  void getAll_shouldReturn200AndWSEFs() throws Exception {
    when(wsefService.getAll()).thenReturn(List.of(wsef));

    mockMvc.perform(get("/api/wsef"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].condition").value("inRange"))
      .andExpect(jsonPath("$[0].windSpeed").value(10.0))
      .andExpect(jsonPath("$[0].maxWindSpeed").value(20.0))
      .andExpect(jsonPath("$[0].forbidden").value(false))
      .andExpect(jsonPath("$[0].fee").value(5.0))
      .andExpect(jsonPath("$[0].vehicleIds").isArray())
      .andExpect(jsonPath("$[0].createdAt")
        .value("2026-01-01T10:00:00Z"))
      .andExpect(jsonPath("$[0].updatedAt")
        .value("2026-01-02T10:00:00Z"));

    verify(wsefService).getAll();
  }

  @Test
  void getById_shouldReturn200AndWSEF() throws Exception {
    when(wsefService.getById(1L)).thenReturn(wsef);

    mockMvc.perform(get("/api/wsef/{id}", 1L))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.condition").value("inRange"))
      .andExpect(jsonPath("$.windSpeed").value(10.0))
      .andExpect(jsonPath("$.maxWindSpeed").value(20.0))
      .andExpect(jsonPath("$.forbidden").value(false));

    verify(wsefService).getById(1L);
  }

  @Test
  void getById_shouldReturn404WhenNotFound() throws Exception {
    when(wsefService.getById(99L))
      .thenThrow(new WSEFNotFoundException(99L));

    mockMvc.perform(get("/api/wsef/{id}", 99L))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail")
        .value("WSEF not found: id=99"));
  }

  @Test
  void create_shouldReturn201AndLocation() throws Exception {
    when(wsefService.create(
      eq(Condition.inRange),
      eq(new BigDecimal("10.0")),
      eq(new BigDecimal("20.0")),
      eq(new BigDecimal("5.0")),
      eq(Set.of(1L))
    )).thenReturn(wsef);

    String request = """
      {
        "condition": "inRange",
        "windSpeed": 10.0,
        "maxWindSpeed": 20.0,
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": [1]
      }
      """;

    mockMvc.perform(post("/api/wsef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isCreated())
      .andExpect(header().string(
        "Location",
        endsWith("/api/wsef/1")
      ))
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.condition").value("inRange"))
      .andExpect(jsonPath("$.windSpeed").value(10.0))
      .andExpect(jsonPath("$.maxWindSpeed").value(20.0))
      .andExpect(jsonPath("$.forbidden").value(false))
      .andExpect(jsonPath("$.fee").value(5.0));

    verify(wsefService).create(
      eq(Condition.inRange),
      eq(new BigDecimal("10.0")),
      eq(new BigDecimal("20.0")),
      eq(new BigDecimal("5.0")),
      eq(Set.of(1L))
    );
  }

  @Test
  void create_shouldResolveForbiddenFeeToNull() throws Exception {
    WSEF forbiddenWSEF = mock(WSEF.class);

    when(forbiddenWSEF.getId()).thenReturn(2L);
    when(forbiddenWSEF.getCondition()).thenReturn(Condition.equal);
    when(forbiddenWSEF.getWindSpeed()).thenReturn(new BigDecimal("10.0"));
    when(forbiddenWSEF.getMaxWindSpeed()).thenReturn(null);
    when(forbiddenWSEF.getFee()).thenReturn(null);
    when(forbiddenWSEF.checkIsForbidden()).thenReturn(true);
    when(forbiddenWSEF.getVehicles()).thenReturn(Set.of());
    when(forbiddenWSEF.getCreatedAt())
      .thenReturn(Instant.parse("2026-01-01T10:00:00Z"));
    when(forbiddenWSEF.getUpdatedAt())
      .thenReturn(Instant.parse("2026-01-01T10:00:00Z"));

    when(wsefService.create(
      eq(Condition.equal),
      eq(new BigDecimal("10.0")),
      isNull(),
      isNull(),
      isNull()
    )).thenReturn(forbiddenWSEF);

    String request = """
      {
        "condition": "equal",
        "windSpeed": 10.0,
        "maxWindSpeed": null,
        "forbidden": true,
        "fee": null,
        "vehicleIds": null
      }
      """;

    mockMvc.perform(post("/api/wsef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.forbidden").value(true))
      .andExpect(jsonPath("$.fee").value(org.hamcrest.Matchers.nullValue()));

    verify(wsefService).create(
      eq(Condition.equal),
      eq(new BigDecimal("10.0")),
      isNull(),
      isNull(),
      isNull()
    );
  }

  @Test
  void create_shouldReturn400WhenFeeIsInconsistent() throws Exception {
    String request = """
      {
        "condition": "equal",
        "windSpeed": 10.0,
        "maxWindSpeed": null,
        "forbidden": true,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post("/api/wsef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(wsefService);
  }

  @Test
  void create_shouldReturn400WhenWindSpeedIsMissing() throws Exception {
    String request = """
      {
        "condition": "equal",
        "maxWindSpeed": null,
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post("/api/wsef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(wsefService);
  }

  @Test
  void create_shouldReturn400WhenWindSpeedExceedsDigitsLimit() throws Exception {
    String request = """
      {
        "condition": "equal",
        "windSpeed": 12345.0,
        "maxWindSpeed": null,
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post("/api/wsef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(wsefService);
  }

  @Test
  void create_shouldReturn400WhenServiceRejectsData() throws Exception {
    when(wsefService.create(
      any(),
      any(),
      any(),
      any(),
      any()
    )).thenThrow(
      new WSEFDataIntegrityViolationException(
        "Range must be specified!"
      )
    );

    String request = """
      {
        "condition": "inRange",
        "windSpeed": 10.0,
        "maxWindSpeed": 20.0,
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post("/api/wsef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.detail")
        .value("Range must be specified!"));
  }

  @Test
  void update_shouldReturn200AndUpdatedWSEF() throws Exception {
    when(wsefService.update(
      eq(1L),
      eq(Condition.inRange),
      eq(new BigDecimal("12.0")),
      eq(new BigDecimal("25.0")),
      eq(new BigDecimal("7.0"))
    )).thenReturn(wsef);

    String request = """
      {
        "condition": "inRange",
        "windSpeed": 12.0,
        "maxWindSpeed": 25.0,
        "forbidden": false,
        "fee": 7.0
      }
      """;

    mockMvc.perform(put("/api/wsef/{id}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.condition").value("inRange"));

    verify(wsefService).update(
      1L,
      Condition.inRange,
      new BigDecimal("12.0"),
      new BigDecimal("25.0"),
      new BigDecimal("7.0")
    );
  }

  @Test
  void update_shouldReturn404WhenWSEFDoesNotExist() throws Exception {
    when(wsefService.update(
      eq(99L),
      any(),
      any(),
      any(),
      any()
    )).thenThrow(new WSEFNotFoundException(99L));

    String request = """
      {
        "condition": "inRange",
        "windSpeed": 12.0,
        "maxWindSpeed": 25.0,
        "forbidden": false,
        "fee": 7.0
      }
      """;

    mockMvc.perform(put("/api/wsef/{id}", 99L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail")
        .value("WSEF not found: id=99"));
  }

  @Test
  void update_shouldReturn400WhenFeeIsInconsistent() throws Exception {
    String request = """
      {
        "condition": "equal",
        "windSpeed": 10.0,
        "maxWindSpeed": null,
        "forbidden": true,
        "fee": 5.0
      }
      """;

    mockMvc.perform(put("/api/wsef/{id}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(wsefService);
  }

  @Test
  void delete_shouldReturn204() throws Exception {
    doNothing().when(wsefService).delete(1L);

    mockMvc.perform(delete("/api/wsef/{id}", 1L))
      .andExpect(status().isNoContent())
      .andExpect(content().string(""));

    verify(wsefService).delete(1L);
  }

  @Test
  void delete_shouldReturn404WhenWSEFDoesNotExist() throws Exception {
    doThrow(new WSEFNotFoundException(99L))
      .when(wsefService).delete(99L);

    mockMvc.perform(delete("/api/wsef/{id}", 99L))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail")
        .value("WSEF not found: id=99"));
  }
}

