package net.java.trial_task.api.controller;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.java.trial_task.application.service.DeliveryFeeService;

@RestController 
@RequestMapping("/api/delivery")
public class DeliveryFeeController {
  private final DeliveryFeeService service;

  public DeliveryFeeController(DeliveryFeeService service) {
    this.service = service;
  }

  @GetMapping
  public BigDecimal calculateFee(
    @RequestParam("city") String city,
    @RequestParam("vehicle") String vehicle,
    @RequestParam(name = "at", required = false) Instant at
  ) {
    return this.service.calculateDeliveryFee(city, vehicle, at);
  }
}
