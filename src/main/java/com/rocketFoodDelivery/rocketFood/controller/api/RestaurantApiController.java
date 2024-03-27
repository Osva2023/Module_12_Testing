package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;
import com.rocketFoodDelivery.rocketFood.exception.*;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.NoSuchElementException;

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
    // TODO

    /**
     * Deletes a restaurant by ID.
     *
     * @param id The ID of the restaurant to delete.
     * @return ResponseEntity with a success message, or a ResourceNotFoundException if the restaurant is not found.
     */
   @DeleteMapping("/api/restaurants/{id}")
public ResponseEntity<Map<String, Object>> deleteRestaurant(@PathVariable int id) {
    Map<String, Object> response = new HashMap<>();
    try {
        // Get the restaurant before deleting it
        Restaurant restaurant = restaurantService.getRestaurant(id);
        // Delete the restaurant
        restaurantService.deleteRestaurant(id);
        // Prepare the success response
        response.put("message", "Success");
        response.put("data", restaurant);
        return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (NoSuchElementException e) {
        // Prepare the error response
        response.put("error", "Resource Not found");
        response.put("details", "Restaurant with id " + id + " not found");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    } catch (Exception e) {
        response.put("message", "Error deleting restaurant");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

    // TODO

    /**
     * Updates an existing restaurant by ID.
     *
     * @param id                    The ID of the restaurant to update.
     * @param restaurantUpdateData  The updated data for the restaurant.
     * @param result                BindingResult for validation.
     * @return ResponseEntity with the updated restaurant's data
     */
    @PutMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> updateRestaurant(@PathVariable("id") int id, @Valid @RequestBody ApiCreateRestaurantDto restaurantUpdateData, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(result.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
    
        Optional<ApiCreateRestaurantDto> updatedRestaurant = restaurantService.updateRestaurant(id, restaurantUpdateData);
    
        if (!updatedRestaurant.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "resource not found");
            response.put("details", "restaurant with id " + id + " not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Success");
        response.put("data", updatedRestaurant.get());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves details for a restaurant, including its average rating, based on the provided restaurant ID.
     *
     * @param id The unique identifier of the restaurant to retrieve.
     * @return ResponseEntity with HTTP 200 OK if the restaurant is found, HTTP 404 Not Found otherwise.
     *
     * @see RestaurantService#findRestaurantWithAverageRatingById(int) for details on retrieving restaurant information.
     */
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
}
