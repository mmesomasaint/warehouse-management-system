package com.warehousemanagementsystem.dto;

import com.warehousemanagementsystem.model.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public class CheckoutResponseDto {
  private final Long orderId;
  private final Store store;
  private final Customer customer;
  private final Employee employee;
  private final OffsetDateTime orderDate;
  private final BigDecimal totalAmount;
  private final List<SalesOrderItem> orderItems;
}
