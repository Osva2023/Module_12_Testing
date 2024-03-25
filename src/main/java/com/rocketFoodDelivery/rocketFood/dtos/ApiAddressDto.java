package com.rocketFoodDelivery.rocketFood.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiAddressDto {
    private int id;

    @JsonProperty("street_address")
    @NotNull
    private String streetAddress;

    @NotNull
    private String city;

    @JsonProperty("postal_code")
    @NotNull
    private String postalCode;
}
