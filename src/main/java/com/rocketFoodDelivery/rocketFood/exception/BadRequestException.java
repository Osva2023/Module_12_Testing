package com.rocketFoodDelivery.rocketFood.exception;

public class BadRequestException extends RuntimeException {
    private String details;

    public BadRequestException(String message, String details) {
        super(message);
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
}