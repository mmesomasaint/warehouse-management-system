package com.warehousemanagementsystem.exception;

public abstract class WarehouseDomainException extends RuntimeException {
    protected WarehouseDomainException(String message) {
        super(message);
    }
}