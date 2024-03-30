package com.rocketFoodDelivery.rocketFood.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Courier {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id",unique = true ,nullable = false)
    private UserEntity userEntity;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "address_id",nullable = false)
    private Address address;

    @ManyToOne(cascade = CascadeType.REMOVE )
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "courierStatus_id",nullable = false)
    private CourierStatus courierStatus ;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    @Email
    private String email;

    @Column(columnDefinition = "boolean default true") // default
    private boolean active;
}
