package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.repository.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductOrderRepository productOrderRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final AddressRepository addressRepository;
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = Logger.getLogger(RestaurantService.class.getName());

    @Autowired
    public RestaurantService(
            RestaurantRepository restaurantRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            ProductOrderRepository productOrderRepository,
            UserRepository userRepository,
            AddressService addressService,
            AddressRepository addressRepository,
            JdbcTemplate jdbcTemplate) { // Add this line
        this.restaurantRepository = restaurantRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.productOrderRepository = productOrderRepository;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.addressRepository = addressRepository;
        this.jdbcTemplate = jdbcTemplate; // And this line
    }

    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }
    

    
    /**
     * Retrieves a restaurant with its details, including the average rating, based
     * on the provided restaurant ID.
     *
     * @param id The unique identifier of the restaurant to retrieve.
     * @return An Optional containing a RestaurantDTO with details such as id, name,
     *         price range, and average rating.
     *         If the restaurant with the given id is not found, an empty Optional
     *         is returned.
     *
     * @see RestaurantRepository#findRestaurantWithAverageRatingById(int) for the
     *      raw query details from the repository.
     */
    public Optional<ApiRestaurantDto> findRestaurantWithAverageRatingById(int id) {
        List<Object[]> restaurant = restaurantRepository.findRestaurantWithAverageRatingById(id);

        if (!restaurant.isEmpty()) {
            Object[] row = restaurant.get(0);
            int restaurantId = (int) row[0];
            String name = (String) row[1];
            int priceRange = (int) row[2];
            double rating = (row[3] != null) ? ((BigDecimal) row[3]).setScale(1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            int roundedRating = (int) Math.ceil(rating);
            ApiRestaurantDto restaurantDto = new ApiRestaurantDto(restaurantId, name, priceRange, roundedRating);
            return Optional.of(restaurantDto);
        } else {
            return Optional.empty();
        }
    }

    public List<ApiRestaurantDto> findRestaurantsByRatingAndPriceRange(Integer rating, Integer priceRange) {
        List<Object[]> restaurants = restaurantRepository.findRestaurantsByRatingAndPriceRange(rating, priceRange);

        List<ApiRestaurantDto> restaurantDtos = new ArrayList<>();

        for (Object[] row : restaurants) {
            int restaurantId = (int) row[0];
            String name = (String) row[1];
            int range = (int) row[2];
            double avgRating = (row[3] != null) ? ((BigDecimal) row[3]).setScale(1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            int roundedAvgRating = (int) Math.ceil(avgRating);
            restaurantDtos.add(new ApiRestaurantDto(restaurantId, name, range, roundedAvgRating));
        }

        return restaurantDtos;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Optional<ApiCreateRestaurantDto> createRestaurant(ApiCreateRestaurantDto restaurantDto) {
        Logger logger = Logger.getLogger("RestaurantService");
        FileHandler fileHandler;

        try {
            fileHandler = new FileHandler("application.log", true);
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            Address address = new Address();
            address.setStreetAddress(restaurantDto.getAddress().getStreetAddress());
            address.setCity(restaurantDto.getAddress().getCity());
            address.setPostalCode(restaurantDto.getAddress().getPostalCode());

            Address savedAddress = addressRepository.save(address);

            // Get the UserEntity
            logger.info("userId: " + restaurantDto.getUserId());
            UserEntity userEntity = entityManager.find(UserEntity.class, restaurantDto.getUserId());
            logger.info("userEntity: " + userEntity);
            if (userEntity == null) {
                throw new RuntimeException("User not found with id: " + restaurantDto.getUserId());
            }
            // Print out userEntity.getId()
            logger.info("userEntity.getId(): " + userEntity.getId());
            // Insert the new Restaurant
            entityManager.createNativeQuery(
                    "INSERT INTO restaurants (name, price_range, phone, email, address_id, user_id) VALUES (?, ?, ?, ?, ?, ?)")
                    .setParameter(1, restaurantDto.getName())
                    .setParameter(2, restaurantDto.getPriceRange())
                    .setParameter(3, restaurantDto.getPhone())
                    .setParameter(4, restaurantDto.getEmail())
                    .setParameter(5, savedAddress.getId())
                    .setParameter(6, userEntity.getId())
                    .executeUpdate();

            // Create and return ApiCreateRestaurantDto
            ApiCreateRestaurantDto createRestaurantDto = new ApiCreateRestaurantDto();
            createRestaurantDto.setUserId(userEntity.getId());
            createRestaurantDto.setName(restaurantDto.getName());
            createRestaurantDto.setPriceRange(restaurantDto.getPriceRange());
            createRestaurantDto.setPhone(restaurantDto.getPhone());
            createRestaurantDto.setEmail(restaurantDto.getEmail());
            createRestaurantDto.setAddress(restaurantDto.getAddress());

            fileHandler.close();

            return Optional.of(createRestaurantDto);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
    // TODO

    /**
     * Finds a restaurant by its ID.
     *
     * @param id The ID of the restaurant to retrieve.
     * @return An Optional containing the restaurant with the specified ID,
     *         or Optional.empty() if no restaurant is found.
     */
    public Optional<Restaurant> findById(int id) {
        return null; // TODO return proper object
    }

    // TODO

    /**
     * Updates an existing restaurant by ID with the provided data.
     *
     * @param id                   The ID of the restaurant to update.
     * @param updatedRestaurantDto The updated data for the restaurant.
     * @return An Optional containing the updated restaurant's information as an
     *         ApiCreateRestaurantDto,
     *         or Optional.empty() if the restaurant with the specified ID is not
     *         found or if an error occurs during the update.
     */

     @Transactional
     public Optional<ApiCreateRestaurantDto> updateRestaurant(int id, ApiCreateRestaurantDto updatedRestaurantDto) {
         try {
            String checkSql = "SELECT COUNT(*) FROM restaurants WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, (rs, rowNum) -> rs.getInt(1), id);
     
             if (count == null || count == 0) {
                 return Optional.empty();
             }
     
             String updateSql = "UPDATE restaurants SET name = ?, price_range = ?, phone = ? WHERE id = ?";
     
             logger.info("Executing SQL query: " + updateSql);
             logger.info("With parameters: name=" + updatedRestaurantDto.getName() + ", price_range=" + updatedRestaurantDto.getPriceRange() + ", phone=" + updatedRestaurantDto.getPhone() + ", id=" + id);
     
             jdbcTemplate.update(updateSql, updatedRestaurantDto.getName(), updatedRestaurantDto.getPriceRange(), updatedRestaurantDto.getPhone(), id);
     
             return Optional.of(updatedRestaurantDto);
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Exception in updateRestaurant", e);
             return Optional.empty();
         }
     }

    // TODO

    /**
     * Deletes a restaurant along with its associated data, including its product
     * orders, orders and products.
     *
     * @param restaurantId The ID of the restaurant to delete.
     */
    @Transactional
    public void deleteRestaurant(int restaurantId) {
        return;
    }
}