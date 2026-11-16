package com.warehousemanagementsystem.controller;

import com.warehousemanagementsystem.dto.CheckoutRequestDto;
import com.warehousemanagementsystem.dto.CheckoutResponseDto;
import com.warehousemanagementsystem.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/api")
public class CheckoutController {
  private final CheckoutService checkoutService;

  public CheckoutController (CheckoutService checkoutService) {
    this.checkoutService = checkoutService;
  }

  @PostMapping("/checkout")
  public ResponseEntity<CheckoutResponseDto> handleCheckout(@RequestBody CheckoutRequestDto request) {
    CheckoutResponseDto response = checkoutService.processCheckout(request);
    return ResponseEntity.ok().body(response);
  }
}