package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "courierStatuses")
public class CourierStatus {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    int id;
    @Column(nullable = false)
    String name;
}
