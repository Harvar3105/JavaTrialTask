package net.java.trial_task.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import net.java.trial_task.api.controller.DeliveryFeeController;
import net.java.trial_task.application.service.DeliveryFeeService;

@WebMvcTest(DeliveryFeeController.class)
class DeliveryFeeControllerTests {
  private static final String URL = "/api/delivery";

  @Autowired private MockMvc mockMvc;
  @MockitoBean private DeliveryFeeService service;

  @Test
  @DisplayName("Without 'at': service receives null, response is 200 with fee")
  void calculateFee_withoutAt_passesNullAndReturnsFee() throws Exception {
    when(service.calculateDeliveryFee("TARTU", "BIKE", null))
      .thenReturn(new BigDecimal("4.00"));

    mockMvc.perform(get(URL)
        .param("city", "TARTU")
        .param("vehicle", "BIKE"))
      .andExpect(status().isOk())
      .andExpect(content().string("4.00"));

    verify(service).calculateDeliveryFee("TARTU", "BIKE", null);
  }

  @Test
  @DisplayName("With ISO-8601 'at': parameter is converted to Instant and passed to service")
  void calculateFee_withAt_passesParsedInstant() throws Exception {
    Instant at = Instant.parse("2026-10-20T10:15:00Z");
    when(service.calculateDeliveryFee("TALLINN", "CAR", at))
      .thenReturn(new BigDecimal("4.00"));

    mockMvc.perform(get(URL)
        .param("city", "TALLINN")
        .param("vehicle", "CAR")
        .param("at", "2026-10-20T10:15:00Z"))
      .andExpect(status().isOk())
      .andExpect(content().string("4.00"));

    verify(service).calculateDeliveryFee("TALLINN", "CAR", at);
  }

  @Test
  @DisplayName("Missing 'city' -> 400, service not called")
  void calculateFee_missingCity_returnsBadRequest() throws Exception {
    mockMvc.perform(get(URL).param("vehicle", "BIKE"))
      .andExpect(status().isBadRequest());

    verify(service, never()).calculateDeliveryFee(any(), any(), any());
  }

  @Test
  @DisplayName("Missing 'vehicle' -> 400, service not called")
  void calculateFee_missingVehicle_returnsBadRequest() throws Exception {
    mockMvc.perform(get(URL).param("city", "TARTU"))
      .andExpect(status().isBadRequest());

    verify(service, never()).calculateDeliveryFee(any(), any(), any());
  }

  @Test
  @DisplayName("Malformed 'at' -> 400 (type conversion failure), service not called")
  void calculateFee_invalidAt_returnsBadRequest() throws Exception {
    mockMvc.perform(get(URL)
        .param("city", "TARTU")
        .param("vehicle", "BIKE")
        .param("at", "not-a-date"))
      .andExpect(status().isBadRequest());

    verify(service, never()).calculateDeliveryFee(any(), any(), any());
  }

  @Test
  @DisplayName("Values are passed to service unchanged (no normalization in controller)")
  void calculateFee_passesRawStringsToService() throws Exception {
    when(service.calculateDeliveryFee(eq("pärnu"), eq("scooter"), eq(null)))
      .thenReturn(new BigDecimal("2.50"));

    mockMvc.perform(get(URL)
        .param("city", "pärnu")
        .param("vehicle", "scooter"))
      .andExpect(status().isOk())
      .andExpect(content().string("2.50"));
  }
}
