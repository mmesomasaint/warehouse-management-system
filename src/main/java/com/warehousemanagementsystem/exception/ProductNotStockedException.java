package com.warehousemanagementsystem.exception;

import java.util.UUID;

public class ProductNotStockedException extends WarehouseDomainException {
    public ProductNotStockedException(UUID storeId, UUID productId) {
        super(String.format("Product ID [%s] is not stocked in store ID [%s]", productId, storeId));
    }
}