package com.rocketFoodDelivery.rocketFood.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiPostAccountDTO {
    String account_type;
    String account_email;
    String account_phone;
}
