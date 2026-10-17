package net.java.trial_task.unit.controller;

import tools.jackson.databind.ObjectMapper;

import net.java.trial_task.api.contracts.VehicleRequest;
import net.java.trial_task.api.vehicle.VehicleController;
import net.java.trial_task.application.domain.entity.data.Vehicle;
import net.java.trial_task.application.exceptions.VehicleAlreadyExistsException;
import net.java.trial_task.application.exceptions.VehicleNotFoundException;
import net.java.trial_task.application.service.VehicleService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
class VehicleControllerTests {
  private static final String BASE_URL = "/api/vehicles";
  private static final Instant NOW = Instant.parse("2026-10-17T10:00:00Z");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private VehicleService service;

  private static Vehicle vehicle(Long id, String type) {
    Vehicle v = new Vehicle(type);
    ReflectionTestUtils.setField(v, "id", id);
    ReflectionTestUtils.setField(v, "createdAt", NOW);
    ReflectionTestUtils.setField(v, "updatedAt", NOW);
    return v;
  }

  private String body(String vehicleType) throws Exception {
    return objectMapper.writeValueAsString(new VehicleRequest(vehicleType));
  }

  @Test
  void getAll_returns200WithList() throws Exception {
    when(service.findAll()).thenReturn(List.of(vehicle(1L, "BIKE"), vehicle(2L, "CAR")));

    mockMvc.perform(get(BASE_URL))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].vehicleType").value("BIKE"))
      .andExpect(jsonPath("$[1].vehicleType").value("CAR"));
  }

  @Test
  void getById_existing_returns200WithBody() throws Exception {
    when(service.findById(1L)).thenReturn(vehicle(1L, "BIKE"));

    mockMvc.perform(get(BASE_URL + "/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.vehicleType").value("BIKE"))
      .andExpect(jsonPath("$.createdAt").value("2026-10-17T10:00:00Z"))
      .andExpect(jsonPath("$.updatedAt").value("2026-10-17T10:00:00Z"));
  }

  @Test
  void getById_missing_returns404ProblemDetail() throws Exception {
    when(service.findById(99L)).thenThrow(new VehicleNotFoundException(99L));

    mockMvc.perform(get(BASE_URL + "/99"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.detail").value("Vehicle not found: id=99"));
  }

  @Test
  void getById_nonNumericId_returns400() throws Exception {
    mockMvc.perform(get(BASE_URL + "/abc"))
      .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }

  @Test
  void create_validRequest_returns201WithLocation() throws Exception {
    when(service.create("BIKE")).thenReturn(vehicle(5L, "BIKE"));

    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body("BIKE")))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", "http://localhost/api/vehicles/5"))
      .andExpect(jsonPath("$.id").value(5))
      .andExpect(jsonPath("$.vehicleType").value("BIKE"));
  }

  @Test
  void create_blankVehicleType_returns400AndDoesNotCallService() throws Exception {
      mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body("  ")))
      .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }

  @Test
  void create_tooLongVehicleType_returns400() throws Exception {
      mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body("x".repeat(65))))
      .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }

  @Test
  void create_missingBody_returns400() throws Exception {
    mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }

  @Test
  void create_duplicate_returns409ProblemDetail() throws Exception {
    when(service.create("BIKE")).thenThrow(new VehicleAlreadyExistsException("BIKE"));

    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body("BIKE")))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.status").value(409))
      .andExpect(jsonPath("$.detail").value("Vehicle already exists: vehicleType=BIKE"));
  }

  @Test
  void update_validRequest_returns200WithBody() throws Exception {
    when(service.update(1L, "MOPED")).thenReturn(vehicle(1L, "MOPED"));

    mockMvc.perform(put(BASE_URL + "/1")
      .contentType(MediaType.APPLICATION_JSON)
      .content(body("MOPED")))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.vehicleType").value("MOPED"));
  }

  @Test
  void update_blankVehicleType_returns400() throws Exception {
    mockMvc.perform(put(BASE_URL + "/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body("")))
      .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }

  @Test
  void update_missing_returns404() throws Exception {
    when(service.update(eq(99L), anyString())).thenThrow(new VehicleNotFoundException(99L));

    mockMvc.perform(put(BASE_URL + "/99")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body("MOPED")))
      .andExpect(status().isNotFound());
  }

  @Test
  void update_duplicate_returns409() throws Exception {
    when(service.update(eq(1L), anyString())).thenThrow(new VehicleAlreadyExistsException("CAR"));

    mockMvc.perform(put(BASE_URL + "/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body("CAR")))
      .andExpect(status().isConflict());
  }

  @Test
  void delete_existing_returns204() throws Exception {
    mockMvc.perform(delete(BASE_URL + "/1"))
      .andExpect(status().isNoContent());
    verify(service).delete(1L);
  }

  @Test
  void delete_missing_returns404() throws Exception {
    doThrow(new VehicleNotFoundException(99L)).when(service).delete(anyLong());

    mockMvc.perform(delete(BASE_URL + "/99"))
      .andExpect(status().isNotFound());
  }
}
