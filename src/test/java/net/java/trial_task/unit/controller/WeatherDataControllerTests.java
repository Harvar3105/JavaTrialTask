package net.java.trial_task.unit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import net.java.trial_task.api.weather.WeatherDataController;
import net.java.trial_task.weather.service.WeatherService;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(WeatherDataController.class)
class WeatherDataControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private WeatherService service;

  @Test
  void updateReturnsAcceptedWithEmptyBody() throws Exception {
    mockMvc.perform(post("/api/weather/updateWeatherData"))
      .andExpect(status().isAccepted())
      .andExpect(content().string(""));

    verify(service).updateWeatherAsync();
  }
}
