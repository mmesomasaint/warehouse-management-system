package com.warehousemanagementsystem.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.warehousemanagementsystem.dto.CreateTransferRequestDto;
import com.warehousemanagementsystem.dto.ReceiveTransferRequestDto;
import com.warehousemanagementsystem.dto.TransferResponseDto;
import com.warehousemanagementsystem.service.InventoryTransferService;

@RestController 
@RequestMapping ("/api/transfers")
public class InventoryTransferController {
  private final InventoryTransferService inventoryTransferService;

  public InventoryTransferController(InventoryTransferService inventoryTransferService) {
    this.inventoryTransferService = inventoryTransferService;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('transfer:create')")
  ResponseEntity<TransferResponseDto> handleCreateTransfer(@Valid @RequestBody CreateTransferRequestDto transferRequest ) {
    TransferResponseDto response = inventoryTransferService.createTransfer(transferRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping ("/{transferId}/ship")
  @PreAuthorize("hasAuthority('transfer:ship')") 
  ResponseEntity<TransferResponseDto> handleShipTransfer(@NotBlank @PathVariable UUID transferId) {
    TransferResponseDto response = inventoryTransferService.shipTransfer(transferId);
    return ResponseEntity.ok(response);
  }

  @PostMapping ("/receiveTransfer")
  @PreAuthorize("hasAuthority('transfer:receive')") 
  ResponseEntity<TransferResponseDto> handleReceiveTransfer(@Valid @RequestBody ReceiveTransferRequestDto request) {
    TransferResponseDto response = inventoryTransferService.receiveTransfer(request);
    return ResponseEntity.ok(response);
  }
}
