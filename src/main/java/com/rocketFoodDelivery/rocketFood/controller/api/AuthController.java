package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.AuthRequestDTO;
import com.rocketFoodDelivery.rocketFood.dtos.AuthResponseErrorDTO;
import com.rocketFoodDelivery.rocketFood.dtos.AuthResponseSuccessDTO;
import com.rocketFoodDelivery.rocketFood.models.Courier;
import com.rocketFoodDelivery.rocketFood.models.Customer;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;
import com.rocketFoodDelivery.rocketFood.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RestController
public class AuthController {
    private CourierRepository courierRepository;
    private CustomerRepository customerRepository;
    @Autowired
    AuthenticationManager authManager;
    @Autowired
    JwtUtil jwtUtil;
    public AuthController(CourierRepository courierRepository,CustomerRepository customerRepository){
        this.courierRepository = courierRepository;
        this.customerRepository = customerRepository;
    }
    @PostMapping("/api/auth")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthRequestDTO request){
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword())
            );
            UserEntity user = (UserEntity) authentication.getPrincipal();
            String accessToken = jwtUtil.generateAccessToken(user);
            Optional<Courier> courier = courierRepository.findByUserEntityId(user.getId());
            Optional<Customer> customer = customerRepository.findByUserEntityId(user.getId());

            AuthResponseSuccessDTO response = new AuthResponseSuccessDTO();
            if(courier.isPresent()){
                response.setCourier_id(courier.get().getId());
            }
            if (customer.isPresent()){
                response.setCustomer_id(customer.get().getId());
            }
            response.setSuccess(true);
            response.setAccessToken(accessToken);
            response.setUser_id(user.getId());
            return ResponseEntity.ok().body(response);
        } catch (BadCredentialsException e) {
            AuthResponseErrorDTO response = new AuthResponseErrorDTO();
            response.setSuccess(false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
