package com.rocketFoodDelivery.rocketFood.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// This object is used to return the a responce to the api calls.// This is  not needed i have created to make the code more organized.
public class ApiResponseDTO {
    private String message;
    private Object data;
}
