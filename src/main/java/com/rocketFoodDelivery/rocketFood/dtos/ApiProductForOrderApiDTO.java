package com.rocketFoodDelivery.rocketFood.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// the order api has a prodect. and this is for that
public class ApiProductForOrderApiDTO {
    int id;
    String product_name;
    int quantity;
    int unit_cost;
    int total_cost;
}
