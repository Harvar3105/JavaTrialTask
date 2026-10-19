package net.java.trial_task.unit.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import net.java.trial_task.api.exceptionHandler.WPEFExceptionHandler;
import net.java.trial_task.api.controller.condition.WPEFController;
import net.java.trial_task.application.domain.entity.condition.WPEF;
import net.java.trial_task.application.domain.enums.Condition;
import net.java.trial_task.application.exceptions.condition.WPEFDataIntegrityViolationException;
import net.java.trial_task.application.exceptions.condition.WPEFNotFoundException;
import net.java.trial_task.application.service.condition.WPEFService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WPEFController.class)
@Import(WPEFExceptionHandler.class)
class WPEFControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockitoBean private WPEFService wpefService;

  private WPEF wpef;

  @BeforeEach
  void setUp() {
    wpef = mock(WPEF.class);

    when(wpef.getId()).thenReturn(1L);
    when(wpef.getCondition()).thenReturn(Condition.related);
    when(wpef.getPhenomenon()).thenReturn("rain");
    when(wpef.getFee()).thenReturn(new BigDecimal("5.0"));
    when(wpef.checkIsForbidden()).thenReturn(false);
    when(wpef.getVehicles()).thenReturn(Set.of());
    when(wpef.getCreatedAt())
      .thenReturn(Instant.parse("2026-01-01T10:00:00Z"));
    when(wpef.getUpdatedAt())
      .thenReturn(Instant.parse("2026-01-02T10:00:00Z"));
  }

  @Test
  void getAll_shouldReturn200AndWPEFs() throws Exception {
    when(wpefService.getAll()).thenReturn(List.of(wpef));

    mockMvc.perform(get("/api/wpef"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].condition").value("related"))
      .andExpect(jsonPath("$[0].phenomenon").value("rain"))
      .andExpect(jsonPath("$[0].forbidden").value(false))
      .andExpect(jsonPath("$[0].fee").value(5.0))
      .andExpect(jsonPath("$[0].vehicleIds").isArray())
      .andExpect(jsonPath("$[0].createdAt")
        .value("2026-01-01T10:00:00Z"))
      .andExpect(jsonPath("$[0].updatedAt")
        .value("2026-01-02T10:00:00Z"));

    verify(wpefService).getAll();
  }

  @Test
  void getById_shouldReturn200AndWPEF() throws Exception {
    when(wpefService.getById(1L)).thenReturn(wpef);

    mockMvc.perform(get("/api/wpef/{id}", 1L))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.condition").value("related"))
      .andExpect(jsonPath("$.phenomenon").value("rain"))
      .andExpect(jsonPath("$.forbidden").value(false))
      .andExpect(jsonPath("$.fee").value(5.0));

    verify(wpefService).getById(1L);
  }

  @Test
  void getById_shouldReturn404WhenNotFound() throws Exception {
    when(wpefService.getById(99L))
      .thenThrow(new WPEFNotFoundException(99L));

    mockMvc.perform(get("/api/wpef/{id}", 99L))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail")
        .value("WPEF not found: id=99"));
  }

  @Test
  void create_shouldReturn201AndLocation() throws Exception {
    when(wpefService.create(
      eq(Condition.related),
      eq("rain"),
      eq(new BigDecimal("5.0")),
      eq(Set.of(1L))
    )).thenReturn(wpef);

    String request = """
      {
        "condition": "related",
        "phenomenon": "rain",
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": [1]
      }
      """;

    mockMvc.perform(post("/api/wpef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isCreated())
      .andExpect(header().string(
        "Location",
        endsWith("/api/wpef/1")
      ))
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.condition").value("related"))
      .andExpect(jsonPath("$.phenomenon").value("rain"))
      .andExpect(jsonPath("$.forbidden").value(false))
      .andExpect(jsonPath("$.fee").value(5.0));

    verify(wpefService).create(
      eq(Condition.related),
      eq("rain"),
      eq(new BigDecimal("5.0")),
      eq(Set.of(1L))
    );
  }

  @Test
  void create_shouldResolveForbiddenFeeToNull() throws Exception {
    WPEF forbiddenWPEF = mock(WPEF.class);

    when(forbiddenWPEF.getId()).thenReturn(2L);
    when(forbiddenWPEF.getCondition()).thenReturn(Condition.related);
    when(forbiddenWPEF.getPhenomenon()).thenReturn("snow");
    when(forbiddenWPEF.getFee()).thenReturn(null);
    when(forbiddenWPEF.checkIsForbidden()).thenReturn(true);
    when(forbiddenWPEF.getVehicles()).thenReturn(Set.of());
    when(forbiddenWPEF.getCreatedAt())
      .thenReturn(Instant.parse("2026-01-01T10:00:00Z"));
    when(forbiddenWPEF.getUpdatedAt())
      .thenReturn(Instant.parse("2026-01-01T10:00:00Z"));

    when(wpefService.create(
      eq(Condition.related),
      eq("snow"),
      isNull(),
      isNull()
    )).thenReturn(forbiddenWPEF);

    String request = """
      {
        "condition": "related",
        "phenomenon": "snow",
        "forbidden": true,
        "fee": null,
        "vehicleIds": null
      }
      """;

    mockMvc.perform(post("/api/wpef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.forbidden").value(true))
      .andExpect(jsonPath("$.fee").value(nullValue()));

    verify(wpefService).create(
      eq(Condition.related),
      eq("snow"),
      isNull(),
      isNull()
    );
  }

  @Test
  void create_shouldReturn400WhenFeeIsInconsistent() throws Exception {
    String request = """
      {
        "condition": "related",
        "phenomenon": "rain",
        "forbidden": true,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post("/api/wpef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(wpefService);
  }

  @Test
  void create_shouldReturn400WhenPhenomenonIsMissing() throws Exception {
    String request = """
      {
        "condition": "related",
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post("/api/wpef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(wpefService);
  }

  @Test
  void create_shouldReturn400WhenPhenomenonExceeds255Characters() throws Exception {

    String phenomenon = "a".repeat(256);

    String request = """
      {
        "condition": "related",
        "phenomenon": "%s",
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": []
      }
      """.formatted(phenomenon);

    mockMvc.perform(post("/api/wpef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(wpefService);
  }

  @Test
  void create_shouldReturn400WhenFeeIsNegative() throws Exception {
    String request = """
      {
        "condition": "related",
        "phenomenon": "rain",
        "forbidden": false,
        "fee": -1.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post("/api/wpef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(wpefService);
  }

  @Test
  void create_shouldReturn400WhenServiceRejectsCondition() throws Exception {

    when(wpefService.create(
      any(),
      anyString(),
      any(),
      any()
    )).thenThrow(
      new WPEFDataIntegrityViolationException(
        "Only 'related' condition is allowed!"
      )
    );

    String request = """
      {
        "condition": "equal",
        "phenomenon": "rain",
        "forbidden": false,
        "fee": 5.0,
        "vehicleIds": []
      }
      """;

    mockMvc.perform(post("/api/wpef")
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.detail")
        .value("Only 'related' condition is allowed!"));
  }

  @Test
  void update_shouldReturn200AndUpdatedWPEF() throws Exception {
    when(wpefService.update(
      eq(1L),
      eq(Condition.related),
      eq("snow"),
      eq(new BigDecimal("7.0"))
    )).thenReturn(wpef);

    String request = """
      {
        "condition": "related",
        "phenomenon": "snow",
        "forbidden": false,
        "fee": 7.0
      }
      """;

    mockMvc.perform(put("/api/wpef/{id}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.condition").value("related"))
      .andExpect(jsonPath("$.phenomenon").value("rain"));

    verify(wpefService).update(
      1L,
      Condition.related,
      "snow",
      new BigDecimal("7.0")
    );
  }

  @Test
  void update_shouldReturn404WhenWPEFDoesNotExist() throws Exception {
    when(wpefService.update(
      eq(99L),
      any(),
      anyString(),
      any()
    )).thenThrow(new WPEFNotFoundException(99L));

    String request = """
      {
        "condition": "related",
        "phenomenon": "snow",
        "forbidden": false,
        "fee": 7.0
      }
      """;

    mockMvc.perform(put("/api/wpef/{id}", 99L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail")
        .value("WPEF not found: id=99"));
  }

  @Test
  void update_shouldReturn400WhenFeeIsInconsistent() throws Exception {
    String request = """
      {
        "condition": "related",
        "phenomenon": "rain",
        "forbidden": true,
        "fee": 5.0
      }
      """;

    mockMvc.perform(put("/api/wpef/{id}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(request))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(wpefService);
  }

  @Test
  void delete_shouldReturn204() throws Exception {
    doNothing().when(wpefService).delete(1L);

    mockMvc.perform(delete("/api/wpef/{id}", 1L))
      .andExpect(status().isNoContent())
      .andExpect(content().string(""));

    verify(wpefService).delete(1L);
  }

  @Test
  void delete_shouldReturn404WhenWPEFDoesNotExist() throws Exception {
    doThrow(new WPEFNotFoundException(99L))
      .when(wpefService).delete(99L);

    mockMvc.perform(delete("/api/wpef/{id}", 99L))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail")
        .value("WPEF not found: id=99"));
  }
}

