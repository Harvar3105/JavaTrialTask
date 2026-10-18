package net.java.trial_task.unit.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import net.java.trial_task.api.contracts.rbf.RBFCreateRequest;
import net.java.trial_task.api.contracts.rbf.RBFUpdateRequest;
import net.java.trial_task.api.rbf.RBFController;
import net.java.trial_task.application.domain.entity.condition.RBF;
import net.java.trial_task.application.exceptions.RBFNotFoundException;
import net.java.trial_task.application.service.RBFService;

@WebMvcTest(RBFController.class)
class RBFControllerTests {

  private static final String BASE = "/api/rbf";
  private static final Instant NOW = Instant.parse("2026-10-17T10:00:00Z");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private RBFService service;

  private static RBF rbf(Long id, Long cityId, Long vehicleTypeId, BigDecimal fee) {
    RBF rbfEntity = new RBF(cityId, vehicleTypeId, fee);
    ReflectionTestUtils.setField(rbfEntity, "id", id);
    ReflectionTestUtils.setField(rbfEntity, "createdAt", NOW);
    ReflectionTestUtils.setField(rbfEntity, "updatedAt", NOW);
    return rbfEntity;
  }

  private String body(Long cityId, Long vehicleTypeId, BigDecimal fee) throws Exception {
    return objectMapper.writeValueAsString(new RBFCreateRequest(cityId, vehicleTypeId, fee));
  }

  private String feeBody(BigDecimal fee) throws Exception {
    return objectMapper.writeValueAsString(new RBFUpdateRequest(fee));
  }

  @Test
  void findAll_returnsList() throws Exception {
    when(service.findAll()).thenReturn(List.of(
      rbf(1L, 1L, 2L, new BigDecimal("4.0")),
      rbf(2L, 1L, 3L, null)));

    mockMvc.perform(get(BASE))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].fee").value(4.0))
      .andExpect(jsonPath("$[1].id").value(2))
      .andExpect(jsonPath("$[1].fee").isEmpty());
  }

  @Test
  void findAll_empty_returnsEmptyArray() throws Exception {
    when(service.findAll()).thenReturn(List.of());

    mockMvc.perform(get(BASE))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void findById_existing_returnsEntity() throws Exception {
    when(service.findById(1L)).thenReturn(rbf(1L, 1L, 2L, new BigDecimal("4.0")));

    mockMvc.perform(get(BASE + "/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.cityId").value(1))
      .andExpect(jsonPath("$.vehicleTypeId").value(2))
      .andExpect(jsonPath("$.fee").value(4.0))
      .andExpect(jsonPath("$.createdAt").value("2026-10-17T10:00:00Z"))
      .andExpect(jsonPath("$.updatedAt").value("2026-10-17T10:00:00Z"));
  }

  @Test
  void findById_absent_returns404() throws Exception {
    when(service.findById(99L)).thenThrow(new RBFNotFoundException(99L));

    mockMvc.perform(get(BASE + "/99"))
      .andExpect(status().isNotFound());
  }

  @Test
  void create_valid_returns201WithLocation() throws Exception {
    when(service.create(1L, 2L, new BigDecimal("3.5")))
      .thenReturn(rbf(7L, 1L, 2L, new BigDecimal("3.5")));

    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body(1L, 2L, new BigDecimal("3.5"))))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", endsWith(BASE + "/7")))
      .andExpect(jsonPath("$.id").value(7))
      .andExpect(jsonPath("$.cityId").value(1))
      .andExpect(jsonPath("$.vehicleTypeId").value(2))
      .andExpect(jsonPath("$.fee").value(3.5));
  }

  @Test
  void create_nullFee_returns201() throws Exception {
    when(service.create(1L, 2L, null)).thenReturn(rbf(7L, 1L, 2L, null));

    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body(1L, 2L, null)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.fee").isEmpty());
  }

  @Test
  void create_nullCityId_returns400() throws Exception {
    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body(null, 2L, BigDecimal.ONE)))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  void create_nullVehicleTypeId_returns400() throws Exception {
    mockMvc.perform(post(BASE)
          .contentType(MediaType.APPLICATION_JSON)
        .content(body(1L, null, BigDecimal.ONE)))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  void create_feeWithTwoFractionDigits_returns400() throws Exception {
    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body(1L, 2L, new BigDecimal("3.55"))))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  void create_feeWithFiveIntegerDigits_returns400() throws Exception {
    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body(1L, 2L, new BigDecimal("12345.0"))))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  void create_malformedJson_returns400() throws Exception {
    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{"))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  void updateFee_valid_returnsUpdatedEntity() throws Exception {
    when(service.updateFee(1L, new BigDecimal("9.9")))
      .thenReturn(rbf(1L, 1L, 2L, new BigDecimal("9.9")));

    mockMvc.perform(put(BASE + "/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(feeBody(new BigDecimal("9.9"))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.fee").value(9.9));
  }

  @Test
  void updateFee_nullFee_returns200() throws Exception {
    when(service.updateFee(1L, null)).thenReturn(rbf(1L, 1L, 2L, null));

    mockMvc.perform(put(BASE + "/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(feeBody(null)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.fee").isEmpty());
  }

  @Test
  void updateFee_invalidScale_returns400() throws Exception {
    mockMvc.perform(put(BASE + "/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(feeBody(new BigDecimal("1.23"))))
      .andExpect(status().isBadRequest());

    verifyNoInteractions(service);
  }

  @Test
  void updateFee_absent_returns404() throws Exception {
    when(service.updateFee(eq(99L), any())).thenThrow(new RBFNotFoundException(99L));

    mockMvc.perform(put(BASE + "/99")
        .contentType(MediaType.APPLICATION_JSON)
        .content(feeBody(BigDecimal.ONE)))
      .andExpect(status().isNotFound());
  }

  @Test
  void delete_existing_returns204() throws Exception {
    mockMvc.perform(delete(BASE + "/1"))
      .andExpect(status().isNoContent());

    verify(service).delete(1L);
  }

  @Test
  void delete_absent_returns404() throws Exception {
    doThrow(new RBFNotFoundException(99L)).when(service).delete(99L);

    mockMvc.perform(delete(BASE + "/99"))
      .andExpect(status().isNotFound());
  }
}