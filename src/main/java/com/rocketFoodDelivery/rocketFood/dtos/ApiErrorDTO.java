package com.rocketFoodDelivery.rocketFood.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

/* Used to return API errors. */
public class ApiErrorDTO {
    String error;
    String details;
}
