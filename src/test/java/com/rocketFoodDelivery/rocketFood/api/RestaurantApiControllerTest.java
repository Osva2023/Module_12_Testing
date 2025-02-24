package com.rocketFoodDelivery.rocketFood.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import org.springframework.jdbc.core.RowMapper;
import java.util.Optional;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.controller.api.RestaurantApiController;
import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiErrorDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiResponseDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

    // @Test
    // public void testUpdateRestaurant() throws Exception {
    //     // Arrange
    //     int restaurantId = 1;
    //     ApiCreateRestaurantDto updatedData = new ApiCreateRestaurantDto();
    //     updatedData.setName("test");
    //     updatedData.setPriceRange(3);
    //     updatedData.setPhone("7275152066");

    //     when(restaurantService.updateRestaurant(eq(restaurantId), any(ApiCreateRestaurantDto.class)))
    //             .thenReturn(Optional.of(updatedData));

    //     // Act
    //     MvcResult result = mockMvc.perform(put("/api/restaurants/{id}", restaurantId)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(new ObjectMapper().writeValueAsString(updatedData)))

    //             .andReturn();

    //     System.out.println(result.getResponse().getContentAsString());

    //     // Assert
    //     verify(restaurantService, times(1)).updateRestaurant(eq(restaurantId), any(ApiCreateRestaurantDto.class));
    // }

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
        RestaurantService restaurantService = mock(RestaurantService.class);

        // Mock service behavior
        when(restaurantService.getRestaurant(anyInt())).thenReturn(new Restaurant());
        doNothing().when(restaurantService).deleteRestaurant(anyInt());
        RestaurantApiController restaurantApiController = new RestaurantApiController(restaurantService);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(restaurantApiController).build();
        // Validate response code and content
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/restaurants/{id}", restaurantId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists()); // Check that the "data" field exists in
                                                                               // the response
    }

    @Test
    public void testDeleteRestaurant_NotFound() throws Exception {
        // Mock data
        int restaurantId = 30;
        RestaurantService restaurantService = mock(RestaurantService.class);

        // Mock service behavior
        when(restaurantService.getRestaurant(anyInt())).thenThrow(new NoSuchElementException());

        // Inject the mocked RestaurantService into RestaurantApiController
        RestaurantApiController restaurantApiController = new RestaurantApiController(restaurantService);

        // MockMvc setup
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(restaurantApiController).build();

        // Validate response code and content
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/restaurants/{id}", restaurantId))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("404 Not Found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details")
                        .value("Restaurant with id " + restaurantId + " not found"));
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

    @Test
    public void testChangeOrderStatus() {
        int orderId = 1;
        String newStatusName = "Delivered";
        OrderStatus newStatus = new OrderStatus();
        newStatus.setName(newStatusName);

        when(restaurantService.changeOrderStatus(orderId, newStatusName)).thenReturn(newStatus);

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", newStatusName);

        ResponseEntity<?> response = restaurantController.changeOrderStatus(orderId, statusMap);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof ApiResponseDTO);
        ApiResponseDTO apiResponse = (ApiResponseDTO) response.getBody();
        assertEquals("Success", apiResponse.getMessage());
    }

    @Test
    public void testChangeOrderStatus_NotFound() {
        int orderId = 1;
        String newStatusName = "Delivered";

        when(restaurantService.changeOrderStatus(orderId, newStatusName))
                .thenThrow(new ResourceNotFoundException("Order with id " + orderId + " not found"));

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", newStatusName);

        ResponseEntity<?> response = restaurantController.changeOrderStatus(orderId, statusMap);

        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof ApiErrorDTO);
        assertEquals("Order with id " + orderId + " not found", ((ApiErrorDTO) response.getBody()).getDetails());
    }

    @Test
    public void testChangeOrderStatus_BadRequest() {
        int orderId = 1;
        String newStatusName = "InvalidStatus";

        when(restaurantService.changeOrderStatus(orderId, newStatusName))
                .thenThrow(new BadRequestException("Invalid or missing parameters", null));

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", newStatusName);

        ResponseEntity<?> response = restaurantController.changeOrderStatus(orderId, statusMap);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof ApiErrorDTO);

    }

    @Test
    public void testGetRestaurantById_Success() {
        int id = 1;
        ApiRestaurantDto restaurant = new ApiRestaurantDto();
        // set properties of restaurant as needed

        when(restaurantService.findRestaurantWithAverageRatingById(id)).thenReturn(Optional.of(restaurant));

        ResponseEntity<?> response = restaurantController.getRestaurantById(id);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof ApiResponseDTO);
        assertEquals(restaurant, ((ApiResponseDTO) response.getBody()).getData());
    }

    @Test
    public void testGetRestaurantById_NotFound() {
        int id = 1;

        when(restaurantService.findRestaurantWithAverageRatingById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            restaurantController.getRestaurantById(id);
        });

        String expectedMessage = String.format("Restaurant with id %d not found", id);
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testGetOrdersByUserTypeAndId_Success() {
        when(restaurantService.getOrdersByUserTypeAndId(anyString(), anyInt()))
                .thenReturn(Collections.singletonList(Map.of("key", "value")));

        ResponseEntity<Object> response = restaurantController.getOrdersByUserTypeAndId("customer", 1);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testGetOrdersByUserTypeAndId_ResourceNotFound() {
        when(restaurantService.getOrdersByUserTypeAndId(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<Object> response = restaurantController.getOrdersByUserTypeAndId("customer", 1);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void testGetOrdersByUserTypeAndId_BadRequest() {
        when(restaurantService.getOrdersByUserTypeAndId(anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException());

        ResponseEntity<Object> response = restaurantController.getOrdersByUserTypeAndId("invalid", 1);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void testCreateOrder_Success() throws Exception {
        // Arrange
        Map<String, Object> order = new HashMap<>();
        order.put("id", 1);
        List<Map<String, Integer>> products = new ArrayList<>();
        Map<String, Integer> product = new HashMap<>();
        product.put("id", 1);
        product.put("quantity", 1);
        products.add(product);

        // Mock the RestaurantService
        RestaurantService restaurantService = mock(RestaurantService.class);
        when(restaurantService.createOrder(anyInt(), anyInt(), anyInt(), anyList())).thenReturn(order);

        // Inject the mocked RestaurantService into RestaurantApiController
        RestaurantApiController restaurantApiController = new RestaurantApiController(restaurantService);

        // MockMvc setup
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(restaurantApiController).build();

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"restaurant_id\":1,\"customer_id\":1,\"courier_id\":1,\"products\":[{\"id\":1,\"quantity\":1}]}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.is("Success")));
    }

    @Test
    public void testCreateOrder_BadRequest() throws Exception {
        // Arrange
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Bad Request");

        // Mock the RestaurantService
        RestaurantService restaurantService = mock(RestaurantService.class);
        when(restaurantService.createOrder(anyInt(), anyInt(), anyInt(), anyList()))
                .thenThrow(new IllegalArgumentException());

        // Inject the mocked RestaurantService into RestaurantApiController
        RestaurantApiController restaurantApiController = new RestaurantApiController(restaurantService);

        // MockMvc setup
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(restaurantApiController).build();

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"restaurant_id\":1,\"customer_id\":1,\"courier_id\":1,\"products\":[{\"id\":1,\"quantity\":1}]}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

}