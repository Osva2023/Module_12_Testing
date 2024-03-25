package com.rocketFoodDelivery.rocketFood.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequestDTO {
    @NotNull
    @Email
    private String email;

    @NotNull
    private String password;
}
