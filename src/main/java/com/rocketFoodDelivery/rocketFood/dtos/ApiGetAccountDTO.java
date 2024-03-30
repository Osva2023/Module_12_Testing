package com.rocketFoodDelivery.rocketFood.dtos;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiGetAccountDTO {
    @Email
    String primary_email;
    @Email
    String account_email;
    String account_phone;
}
