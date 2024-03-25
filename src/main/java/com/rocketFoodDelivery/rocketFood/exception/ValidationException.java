package com.rocketFoodDelivery.rocketFood.exception;
import org.springframework.validation.Errors;

import lombok.Getter;

public class ValidationException extends RuntimeException {
    @Getter
    private final Errors errors;

    public ValidationException(Errors errors) {
        this.errors = errors;
    }

}
