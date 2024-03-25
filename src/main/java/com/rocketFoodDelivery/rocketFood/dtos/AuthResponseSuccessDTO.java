package com.rocketFoodDelivery.rocketFood.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthResponseSuccessDTO {
    private String accessToken;
    private boolean success;
    private int user_id;
    private int customer_id;
    private int courier_id;
}
