package com.warehousemanagementsystem.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.*;

import com.warehousemanagementsystem.dto.CreateTransferRequestDto;
import com.warehousemanagementsystem.dto.ReceiveTransferRequestDto;
import com.warehousemanagementsystem.dto.TransferResponseDto;
import com.warehousemanagementsystem.service.InventoryTransferService;

@RestController 
@RequestMapping ("/api/transfer")
public class InventoryTransferController {
  private final InventoryTransferService inventoryTransferService;

  public InventoryTransferController(InventoryTransferService inventoryTransferService) {
    this.inventoryTransferService = inventoryTransferService;
  }

  @PostMapping ("/createtransfer")
  ResponseEntity<TransferResponseDto> handleCreateTransfer(@Valid @RequestBody CreateTransferRequestDto transferRequest ) {
    TransferResponseDto response = inventoryTransferService.createTransfer(transferRequest);
    return ResponseEntity.ok().body(response);
  }

  @PostMapping ("/shiptransfer/{id}") 
  ResponseEntity<TransferResponseDto> handleShipTransfer(@NotBlank @PathVariable UUID transferId) {
    TransferResponseDto response = inventoryTransferService.shipTransfer(transferId);
    return ResponseEntity.ok().body(response);
  }

  @PostMapping ("/receiveTransfer")
  ResponseEntity<TransferResponseDto> handleReceiveTransfer(@Valid @RequestBody ReceiveTransferRequestDto request) {
    TransferResponseDto response = inventoryTransferService.receiveTransfer(request);
    return ResponseEntity.ok().body(response);
  }
}
