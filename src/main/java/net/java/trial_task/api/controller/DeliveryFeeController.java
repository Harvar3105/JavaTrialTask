package net.java.trial_task.api.controller;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.java.trial_task.application.service.DeliveryFeeService;

@Tag(name = "Delivery fee", description = "Calculation of the total delivery fee: RBF + ATEF + WSEF + WPEF")
@RestController 
@RequestMapping("/api/delivery")
public class DeliveryFeeController {
  private final DeliveryFeeService service;

  public DeliveryFeeController(DeliveryFeeService service) {
    this.service = service;
  }

  @GetMapping
  @Operation(
    summary = "Calculate delivery fee",
    description = """
      Returns the total delivery fee in EUR for the given city and vehicle type: the regional base fee (RBF) \
      plus the extra fees for air temperature (ATEF), wind speed (WSEF) and weather phenomenon (WPEF). \
      The calculation uses the latest weather observation of the station matching the city, or, if 'at' is \
      provided, the observation valid at that moment. If any stage yields a null fee, usage of the vehicle \
      type is forbidden and an error is returned instead of a fee.""")
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Total delivery fee in EUR",
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = java.math.BigDecimal.class),
        examples = @ExampleObject(value = "4.0"))),
    @ApiResponse(
      responseCode = "400",
      description = "Missing required parameter or malformed 'at' value",
      content = @Content),
    @ApiResponse(
      responseCode = "404",
      description = "Unknown city or vehicle type, or no fee rule or weather data available",
      content = @Content),
    @ApiResponse(
      responseCode = "422",
      description = "Usage of the selected vehicle type is forbidden",
      content = @Content)
  })
  public BigDecimal calculateFee(
    @Parameter(
      name = "city",
      in = ParameterIn.QUERY,
      required = true,
      description = "City name",
      example = "Tartu")
    @RequestParam("city")
    String city,

    @Parameter(
      name = "vehicle",
      in = ParameterIn.QUERY,
      required = true,
      description = "Vehicle type name",
      example = "Bike")
    @RequestParam("vehicle")
    String vehicle,

    @Parameter(
      name = "at",
      in = ParameterIn.QUERY,
      required = false,
      description = "Moment in time, UTC (ISO-8601), for which the fee is calculated. If omitted, the latest weather data is used.",
      example = "2026-10-24T12:00:00Z")
    @RequestParam(name = "at", required = false)
    Instant at
  ) {
    return this.service.calculateDeliveryFee(city, vehicle, at);
  }
}
