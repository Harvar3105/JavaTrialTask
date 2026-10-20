package net.java.trial_task.unit.controller;

import tools.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import net.java.trial_task.api.controller.data.CityController;
import net.java.trial_task.api.contracts.data.city.CityRequest;
import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.exceptions.data.CityNotFoundException;
import net.java.trial_task.application.service.data.CityService;

@WebMvcTest(CityController.class)
class CityControllerTests {
  private static final String BASE = "/api/cities";
  private static final Instant NOW = Instant.parse("2026-10-17T10:00:00Z");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private CityService service;

  private static City city(Long id, String name) {
    City city = new City(name);
    ReflectionTestUtils.setField(city, "id", id);
    ReflectionTestUtils.setField(city, "createdAt", NOW);
    ReflectionTestUtils.setField(city, "updatedAt", NOW);
    return city;
  }

  private String body(String cityName) throws Exception {
    return objectMapper.writeValueAsString(new CityRequest(cityName));
  }

  @Test
  void create_valid_returns201WithLocation() throws Exception {
    when(service.create("Tallinn")).thenReturn(city(1L, "Tallinn"));

    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body("Tallinn")))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith(BASE + "/1")))
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.cityName").value("Tallinn"));
  }

  @Test
  void create_blankName_returns400() throws Exception {
    mockMvc.perform(post(BASE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body(" ")))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }

  @Test
  void create_missingName_returns400() throws Exception {
    mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{}"))
      .andExpect(status().isBadRequest());
  }

  @Test
  void findAll_returnsList() throws Exception {
    when(service.findAll()).thenReturn(List.of(city(1L, "Tallinn"), city(2L, "Tartu")));

    mockMvc.perform(get(BASE))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[1].cityName").value("Tartu"));
  }

  @Test
  void findById_existing_returns200() throws Exception {
    when(service.findById(1L)).thenReturn(city(1L, "Pärnu"));

    mockMvc.perform(get(BASE + "/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.cityName").value("Pärnu"));
  }

  @Test
  void findById_missing_returns404() throws Exception {
    when(service.findById(99L)).thenThrow(new CityNotFoundException(99L));

    mockMvc.perform(get(BASE + "/99"))
      .andExpect(status().isNotFound());
  }

  @Test
  void update_valid_returns200() throws Exception {
    when(service.update(eq(1L), any(String.class))).thenReturn(city(1L, "New"));

    mockMvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(body("New")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.cityName").value("New"));
  }

  @Test
  void update_blankName_returns400() throws Exception {
    mockMvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(body("")))
      .andExpect(status().isBadRequest());
  }

  @Test
  void update_missing_returns404() throws Exception {
    when(service.update(eq(99L), any(String.class))).thenThrow(new CityNotFoundException(99L));

    mockMvc.perform(put(BASE + "/99").contentType(MediaType.APPLICATION_JSON).content(body("New")))
      .andExpect(status().isNotFound());
  }

  @Test
  void delete_existing_returns204() throws Exception {
    mockMvc.perform(delete(BASE + "/1"))
      .andDo(print())
      .andExpect(status().isNoContent());

    verify(service).delete(1L);
  }

  @Test
  void delete_missing_returns404() throws Exception {
    doThrow(new CityNotFoundException(99L)).when(service).delete(99L);

    mockMvc.perform(delete(BASE + "/99"))
      .andDo(print())
      .andExpect(status().isNotFound());
  }
}