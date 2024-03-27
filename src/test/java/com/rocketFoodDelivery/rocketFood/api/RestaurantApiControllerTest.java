package com.rocketFoodDelivery.rocketFood.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;

import java.util.Optional;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.controller.api.RestaurantApiController;
import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RestaurantApiControllerTest {

    @InjectMocks
    private RestaurantApiController restaurantController;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateRestaurant_Success() throws Exception {
        ApiAddressDto inputAddress = new ApiAddressDto(1, "123 Wellington St.", "Montreal", "H1H2H2");
        ApiCreateRestaurantDto inputRestaurant = new ApiCreateRestaurantDto(1, 4, "Villa wellington", 2, "5144154415", "reservations@villawellington.com", inputAddress);
        
        ArgumentCaptor<ApiCreateRestaurantDto> captor = ArgumentCaptor.forClass(ApiCreateRestaurantDto.class);
        // Mock service behavior
        when(restaurantService.createRestaurant(captor.capture())).thenReturn(Optional.of(inputRestaurant));

        // Validate response code and content
        mockMvc.perform(MockMvcRequestBuilders.post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(inputRestaurant)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(inputRestaurant.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.phone").value(inputRestaurant.getPhone()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(inputRestaurant.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.city").value(inputRestaurant.getAddress().getCity()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.street_address").value(inputRestaurant.getAddress().getStreetAddress()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.postal_code").value(inputRestaurant.getAddress().getPostalCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.user_id").value(inputRestaurant.getUserId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.price_range").value(inputRestaurant.getPriceRange()));
    }

    @Test
    public void testUpdateRestaurant_Success() throws Exception {
        // Mock data
        int restaurantId = 1;
        ApiCreateRestaurantDto updatedData = new ApiCreateRestaurantDto();
        updatedData.setName("Updated Namaste");
        updatedData.setPriceRange(2);
        updatedData.setPhone("555-1234");

        // Mock service behavior
        when(restaurantService.updateRestaurant(restaurantId, updatedData))
                .thenReturn(Optional.of(updatedData));

        // Validate response code and content
        mockMvc.perform(MockMvcRequestBuilders.put("/api/restaurants/{id}", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updatedData)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("Updated Namaste"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.price_range").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.phone").value("555-1234"));
    }
@Test
public void testGetAllRestaurantsWithParameters() throws Exception {
    // Define the parameters
    Integer rating = 3;
    Integer priceRange = 2;

    // Mock the service method
    when(restaurantService.findRestaurantsByRatingAndPriceRange(rating, priceRange)).thenReturn(new ArrayList<>());

    // Perform the GET request with parameters and check the status
    mockMvc.perform(MockMvcRequestBuilders.get("/api/restaurants")
            .param("rating", rating.toString())
            .param("price_range", priceRange.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
}
@Test
public void testGetAllRestaurantsWithInvalidParameters() throws Exception {
    // Define invalid parameters
    Integer rating = 6; // Invalid as rating should be between 1 and 5
    Integer priceRange = 4; // Invalid as price range should be between 1 and 3

    // Perform the GET request with invalid parameters and check the status
    mockMvc.perform(MockMvcRequestBuilders.get("/api/restaurants")
            .param("rating", rating.toString())
            .param("price_range", priceRange.toString())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
}

@Test
public void testDeleteRestaurant_Success() throws Exception {
    // Mock data
    int restaurantId = 30;

    // Mock service behavior
    doNothing().when(restaurantService).deleteRestaurant(restaurantId);

    // Validate response code and content
    mockMvc.perform(MockMvcRequestBuilders.delete("/api/restaurants/{id}", restaurantId))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Success"));
}
@Test
public void testGetProductsForRestaurant() {
    int restaurantId = 1;
    Map<String, Object> product = new HashMap<>();
    product.put("id", 1);
    product.put("name", "Cheeseburger");
    product.put("cost", 10.0);
    when(restaurantService.getProductsForRestaurant(restaurantId)).thenReturn(Collections.singletonList(product));

    ResponseEntity<?> response = restaurantController.getProductsForRestaurant(restaurantId);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(Collections.singletonList(product), response.getBody());
}
@Test
public void testGetProductsForRestaurant_NotFound() {
    int restaurantId = 1;
    when(restaurantService.getProductsForRestaurant(restaurantId)).thenReturn(Collections.emptyList());

    ResponseEntity<?> response = restaurantController.getProductsForRestaurant(restaurantId);

    assertEquals(404, response.getStatusCode().value());
}

}