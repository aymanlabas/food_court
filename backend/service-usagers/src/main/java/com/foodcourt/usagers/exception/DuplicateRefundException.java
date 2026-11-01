package com.foodcourt.usagers.exception;

public class DuplicateRefundException extends RuntimeException {
    public DuplicateRefundException(String message) {
        super(message);
    }
}

