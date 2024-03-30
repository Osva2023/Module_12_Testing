package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;
import com.rocketFoodDelivery.rocketFood.exception.*;
import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Optional;

@RestController
public class RestaurantApiController {
    private RestaurantService restaurantService;

    @Autowired
    public RestaurantApiController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

  
   @PostMapping("/api/restaurants")
public ResponseEntity<Map<String, Object>> createRestaurant(@RequestBody ApiCreateRestaurantDto restaurantDto) {
    Optional<ApiCreateRestaurantDto> createdRestaurantDto = restaurantService.createRestaurant(restaurantDto);
    Map<String, Object> response = new HashMap<>();
    if (createdRestaurantDto.isPresent()) {
        response.put("message", "Success");
        response.put("data", createdRestaurantDto.get());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    } else {
        response.put("message", "Invalid or missing parameters");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
   
    @DeleteMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> deleteRestaurant(@PathVariable int id) {
        try {
            // Get the restaurant before deleting it
            Restaurant restaurant = restaurantService.getRestaurant(id);
            // Delete the restaurant
            restaurantService.deleteRestaurant(id);
            // Prepare the success response
            return ResponseBuilder.buildOkResponse(restaurant);
        } catch (NoSuchElementException e) {
            // Prepare the error response
            return ResponseBuilder.buildResourceNotFoundExceptionResponse(new ResourceNotFoundException("Restaurant with id " + id + " not found"));
        }
    }

  
    @PutMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> updateRestaurant(@PathVariable("id") int id, @Valid @RequestBody ApiCreateRestaurantDto restaurantUpdateData, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseBuilder.buildBadRequestExceptionResponse(new BadRequestException("Validation failed", result.getAllErrors().toString()));
        }
    
        Optional<ApiCreateRestaurantDto> updatedRestaurant = restaurantService.updateRestaurant(id, restaurantUpdateData);
    
        if (!updatedRestaurant.isPresent()) {
            return ResponseBuilder.buildResourceNotFoundExceptionResponse(new ResourceNotFoundException("Restaurant with id " + id + " not found"));
        }
    
        return ResponseBuilder.buildOkResponse(updatedRestaurant.get());
    }

    @GetMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> getRestaurantById(@PathVariable int id) {
        Optional<ApiRestaurantDto> restaurantWithRatingOptional = restaurantService.findRestaurantWithAverageRatingById(id);
        if (!restaurantWithRatingOptional.isPresent()) throw new ResourceNotFoundException(String.format("Restaurant with id %d not found", id));
        return ResponseBuilder.buildOkResponse(restaurantWithRatingOptional.get());
    }

  

     @GetMapping("/api/restaurants")
     public ResponseEntity<Object> getAllRestaurants(
         @RequestParam(name = "rating", required = false) Integer rating,
         @RequestParam(name = "price_range", required = false) Integer priceRange) {
     
         // Validate the parameters
         if ((rating != null && (rating < 1 || rating > 5)) || 
             (priceRange != null && (priceRange < 1 || priceRange > 3))) {
             return new ResponseEntity<>("Invalid parameters", HttpStatus.BAD_REQUEST);
         }
     
         return ResponseBuilder.buildOkResponse(restaurantService.findRestaurantsByRatingAndPriceRange(rating, priceRange));
     }
     @GetMapping("/api/products")
public ResponseEntity<?> getProductsForRestaurant(@RequestParam int restaurant) {
    List<Map<String, Object>> products = restaurantService.getProductsForRestaurant(restaurant);
    if (products.isEmpty()) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Resource not found");
        error.put("details", "Products from the restaurant " + restaurant + " not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(products, HttpStatus.OK);
}
@PostMapping("/api/{order_id}/status")
public ResponseEntity<?> changeOrderStatus(@PathVariable("order_id") int orderId, @RequestBody Map<String, String> body) {
    try {
        String status = body.get("status");
        OrderStatus newStatus = restaurantService.changeOrderStatus(orderId, status);
        return ResponseBuilder.buildOkResponse(newStatus);
    } catch (BadRequestException ex) {
        return ResponseBuilder.buildBadRequestExceptionResponse(ex);
    } catch (ResourceNotFoundException ex) {
        return ResponseBuilder.buildResourceNotFoundExceptionResponse(ex);
    }
}


@GetMapping("/api/orders")
public ResponseEntity<Object> getOrdersByUserTypeAndId(@RequestParam String type, @RequestParam int id) {
    try {
        List<Map<String, Object>> orders = restaurantService.getOrdersByUserTypeAndId(type, id);
        if (orders.isEmpty()) {
            return ResponseBuilder.buildResourceNotFoundExceptionResponse(new ResourceNotFoundException("No orders found"));
        }
        return ResponseBuilder.buildOkResponse(orders);
    } catch (IllegalArgumentException e) {
        return ResponseBuilder.buildBadRequestExceptionResponse(new BadRequestException("Invalid or missing parameters", null));
    }
}
@PostMapping("/api/orders")
public ResponseEntity<Object> createOrder(@RequestBody Map<String, Object> request) {
    try {
        if (!request.containsKey("courier_id")) {
            return ResponseBuilder.buildBadRequestExceptionResponse(new BadRequestException("Courier id is missing", null));
        }

        int courierId = (int) request.get("courier_id"); // Extract the courier_id from the request

        // Check if courierId exists
        if (courierId <= 0) {
            return ResponseBuilder.buildBadRequestExceptionResponse(new BadRequestException("Courier id does not exist", null));
        }

        int restaurantId = (int) request.get("restaurant_id");
        int customerId = (int) request.get("customer_id");
        List<Map<String, Integer>> products = (List<Map<String, Integer>>) request.get("products");

        // Try to create the order
        Map<String, Object> order = restaurantService.createOrder(restaurantId, customerId, courierId, products); 

        // If the order is created successfully, return a 200 OK response with the order data
        return ResponseBuilder.buildOkResponse(order);
    } catch (IllegalArgumentException e) {
        // If the parameters are invalid or missing, return a 400 Bad Request response with an error message
        return ResponseBuilder.buildBadRequestExceptionResponse(new BadRequestException("Invalid or missing parameters", null));
    }
}



}
