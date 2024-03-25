package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;
import com.rocketFoodDelivery.rocketFood.exception.*;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class RestaurantApiController {
    private RestaurantService restaurantService;

    @Autowired
    public RestaurantApiController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // TODO

    /**
     * Creates a new restaurant.
     *
     * @param restaurant The data for the new restaurant.
     * @return ResponseEntity with the created restaurant's data, or a BadRequestException if creation fails.
     */
    @PostMapping("/api/restaurants")
    public ResponseEntity<Object> createRestaurant(@RequestBody ApiCreateRestaurantDto restaurant) {
        return null; // TODO return proper object
    }
    
    // TODO

    /**
     * Deletes a restaurant by ID.
     *
     * @param id The ID of the restaurant to delete.
     * @return ResponseEntity with a success message, or a ResourceNotFoundException if the restaurant is not found.
     */
    @DeleteMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> deleteRestaurant(@PathVariable int id){
        return null; // TODO return proper object
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
        return null; // TODO return proper object
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

    /**
     * Returns a list of restaurants given a rating and price range
     *
     * @param rating integer from 1 to 5 (optional)
     * @param priceRange integer from 1 to 3 (optional)
     * @return A list of restaurants that match the specified criteria
     * 
     * @see RestaurantService#findRestaurantsByRatingAndPriceRange(Integer, Integer) for details on retrieving restaurant information.
     */

    @GetMapping("/api/restaurants")
    public ResponseEntity<Object> getAllRestaurants(
        @RequestParam(name = "rating", required = false) Integer rating,
        @RequestParam(name = "price_range", required = false) Integer priceRange) {
        return ResponseBuilder.buildOkResponse(restaurantService.findRestaurantsByRatingAndPriceRange(rating, priceRange));
    }
}
