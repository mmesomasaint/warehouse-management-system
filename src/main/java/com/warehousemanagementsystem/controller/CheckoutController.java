package com.warehousemanagementsystem.controller;

import com.warehousemanagementsystem.dto.CheckoutRequestDto;
import com.warehousemanagementsystem.dto.CheckoutResponseDto;
import com.warehousemanagementsystem.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping ("/api/checkout")
public class CheckoutController {
  private final CheckoutService checkoutService;

  public CheckoutController (CheckoutService checkoutService) {
    this.checkoutService = checkoutService;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('sales:checkout')")
  public ResponseEntity<CheckoutResponseDto> handleCheckout(@Valid @RequestBody CheckoutRequestDto request) {
    CheckoutResponseDto response = checkoutService.processCheckout(request);
    return ResponseEntity.ok(response);
  }
}