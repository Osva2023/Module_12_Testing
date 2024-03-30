package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.repository.*;
import com.rocketFoodDelivery.rocketFood.models.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.models.Courier;
import com.rocketFoodDelivery.rocketFood.models.Customer;
import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
    private OrderStatusRepository orderStatusRepository;
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
            OrderStatusRepository orderStatusRepository, // Add this line
            AddressRepository addressRepository,
            JdbcTemplate jdbcTemplate) {
        this.restaurantRepository = restaurantRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.productOrderRepository = productOrderRepository;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.orderStatusRepository = orderStatusRepository; // And this line
        this.addressRepository = addressRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }

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

    public Restaurant getRestaurant(int id) {
        String sql = "SELECT * FROM restaurants WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new RestaurantRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchElementException("Restaurant with id " + id + " not found");
        }
    }

    private static class RestaurantRowMapper implements RowMapper<Restaurant> {
        @Override
        public Restaurant mapRow(ResultSet rs, int rowNum) throws SQLException {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(rs.getInt("id"));
            restaurant.setName(rs.getString("name"));
            restaurant.setPriceRange(rs.getInt("price_range"));

            return restaurant;
        }
    }

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
            logger.info("With parameters: name=" + updatedRestaurantDto.getName() + ", price_range="
                    + updatedRestaurantDto.getPriceRange() + ", phone=" + updatedRestaurantDto.getPhone() + ", id="
                    + id);

            jdbcTemplate.update(updateSql, updatedRestaurantDto.getName(), updatedRestaurantDto.getPriceRange(),
                    updatedRestaurantDto.getPhone(), id);

            return Optional.of(updatedRestaurantDto);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in updateRestaurant", e);
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteRestaurant(int restaurantId) {
        logger.log(Level.INFO, "Deleting restaurant with id {0}", restaurantId);

        String deleteOrdersSql = "DELETE FROM orders WHERE restaurant_id = ?";
        jdbcTemplate.update(deleteOrdersSql, restaurantId);
        logger.log(Level.INFO, "Deleted orders for restaurant with id {0}", restaurantId);

        String deleteProductsSQL = "DELETE FROM products WHERE restaurant_id = ?";
        jdbcTemplate.update(deleteProductsSQL, restaurantId);
        logger.log(Level.INFO, "Deleted products for restaurant with id {0}", restaurantId);

        String getRestaurantAddressIdSql = "SELECT address_id FROM restaurants WHERE id = ?";
        Integer addressId = jdbcTemplate.queryForObject(getRestaurantAddressIdSql, Integer.class, restaurantId);
        logger.log(Level.INFO, "Got address id {0} for restaurant with id {1}",
                new Object[] { addressId, restaurantId });

        String deleteRestaurantSql = "DELETE FROM restaurants WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(deleteRestaurantSql, restaurantId);
        if (rowsAffected == 0) {
            throw new NoSuchElementException("Restaurant with id " + restaurantId + " not found");
        }
        logger.log(Level.INFO, "Deleted restaurant with id {0}", restaurantId);

        if (addressId != null) {
            String deleteAddressSql = "DELETE FROM addresses WHERE id = ?";
            try {
                jdbcTemplate.update(deleteAddressSql, addressId);
                logger.log(Level.INFO, "Deleted address with id {0}", addressId);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error deleting address with id " + addressId, e);
            }
        }
    }

    public List<Map<String, Object>> getProductsForRestaurant(int restaurantId) {
        String getProductsSql = "SELECT id, name, cost FROM products WHERE restaurant_id = ?";
        return jdbcTemplate.queryForList(getProductsSql, restaurantId);
    }

    public OrderStatus changeOrderStatus(int orderId, String newStatusName) {
        Optional<OrderStatus> optionalStatus = orderStatusRepository.findByName(newStatusName);
        if (!optionalStatus.isPresent()) {
            throw new BadRequestException("Invalid or missing parameters", "Details about the error...");
        }
        OrderStatus newStatus = optionalStatus.get();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id " + orderId + " not found"));
        order.setOrder_status(newStatus);
        orderRepository.save(order);

        return newStatus;
    }

    public List<Map<String, Object>> getOrdersByUserTypeAndId(String type, int id) {
        String sql = "SELECT o.id, c.id as customer_id, c.email as customer_email, " +
                "CONCAT(a.street_address, ', ', a.city, ', ', a.postal_code) as customer_address, " +
                "r.id as restaurant_id, r.name as restaurant_name, " +
                "CONCAT(ra.street_address, ', ', ra.city, ', ', ra.postal_code) as restaurant_address, " +
                "cour.id as courier_id, u.name as courier_name, os.name as status, " +
                "p.id as product_id, p.name as product_name, op.product_quantity, p.cost as unit_cost, " +
                "(op.product_quantity * p.cost) as total_cost " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.id " +
                "JOIN addresses a ON c.address_id = a.id " +
                "JOIN restaurants r ON o.restaurant_id = r.id " +
                "JOIN addresses ra ON r.address_id = ra.id " +
                "JOIN courier cour ON o.courier_id = cour.id " +
                "JOIN users u ON cour.user_id = u.id " +
                "JOIN order_statuses os ON o.status_id = os.id " +
                "JOIN product_orders op ON o.id = op.order_id " +
                "JOIN products p ON op.product_id = p.id ";

        if ("customer".equalsIgnoreCase(type)) {
            sql += "WHERE c.id = ?";
        } else if ("courier".equalsIgnoreCase(type)) {
            sql += "WHERE cour.id = ?";
        } else if ("restaurant".equalsIgnoreCase(type)) {
            sql += "WHERE r.id = ?";
        } else {
            throw new IllegalArgumentException("Invalid user type");
        }

        return jdbcTemplate.queryForList(sql, id);
    }

    public Map<String, Object> createOrder(int restaurantId, int customerId, int courierId, List<Map<String, Integer>> products) {
        if (restaurantId == 0 || customerId == 0 || courierId == 0 || products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Missing parameters for creating order");
        }
        // Fetch the restaurant_rating for the given restaurant_id from the orders table
        String fetchRatingSql = "SELECT restaurant_rating FROM orders WHERE restaurant_id = " + restaurantId;
        List<Integer> restaurantRatings = jdbcTemplate.query(fetchRatingSql,
                (rs, rowNum) -> rs.getInt("restaurant_rating"));
        if (restaurantRatings.isEmpty()) {
            throw new IllegalArgumentException("No rating found for restaurant with ID: " + restaurantId);
        }
        Integer restaurantRating = restaurantRatings.stream().mapToInt(Integer::intValue).sum()
                / restaurantRatings.size();
    
        // Fetch the customer_id for the given customer_id from the orders table
        String fetchCustomerSql = "SELECT customer_id FROM orders WHERE customer_id = " + customerId;
        List<Integer> fetchedCustomerIds = jdbcTemplate.query(fetchCustomerSql,
                (rs, rowNum) -> rs.getInt("customer_id"));
        if (fetchedCustomerIds.isEmpty()) {
            throw new IllegalArgumentException("No customer found with ID: " + customerId);
        }
        Integer fetchedCustomerId = fetchedCustomerIds.get(0); // Assuming there will always be at least one matching
    
        // Include the restaurant_rating, customer_id, and courier_id when creating a new order
        String insertOrderSql = "INSERT INTO orders (restaurant_id, customer_id, courier_id, restaurant_rating, status_id) VALUES (?, ?, ?, ?, 1)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, restaurantId);
            ps.setInt(2, fetchedCustomerId);
            ps.setInt(3, courierId);
            ps.setInt(4, restaurantRating);
            return ps;
        }, keyHolder);
    
        int orderId = keyHolder.getKey().intValue();
    
        String insertProductOrderSql = "INSERT INTO product_orders (order_id, product_id, product_quantity) VALUES (?, ?, ?)";
        for (Map<String, Integer> product : products) {
            jdbcTemplate.update(insertProductOrderSql, orderId, product.get("id"), product.get("quantity"));
        }
    
        // Fetch the details of the created order
        Map<String, Object> createdOrder = getOrderById(orderId);
    
        // Filter the order details to include only the newly created order
    
        return createdOrder;
    }
    public Map<String, Object> getOrderById(int orderId) {
        String sql = "SELECT orders.*, restaurants.name AS restaurant_name, customerUsers.name AS customer_name, courierUsers.name AS courier_name " +
                     "FROM orders " +
                     "JOIN restaurants ON orders.restaurant_id = restaurants.id " +
                     "JOIN customers ON orders.customer_id = customers.id " +
                     "JOIN users AS customerUsers ON customers.user_id = customerUsers.id " +
                     "JOIN courier ON orders.courier_id = courier.id " +
                     "JOIN users AS courierUsers ON courier.user_id = courierUsers.id " +
                     "WHERE orders.id = ?";
        return jdbcTemplate.queryForMap(sql, orderId);
    }
}