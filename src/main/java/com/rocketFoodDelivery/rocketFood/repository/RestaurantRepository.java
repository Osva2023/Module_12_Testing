package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
    Optional<Restaurant> findByUserEntityId(int id);
    List<Restaurant> findAll();

    /**
     * Finds a restaurant by its ID along with the calculated average rating rounded up to the ceiling.
     *
     * @param restaurantId The ID of the restaurant to retrieve.
     * @return A list of Object arrays representing the selected columns from the query result.
     *         Each Object array corresponds to the restaurant's information.
     *         An empty list is returned if no restaurant is found with the specified ID.
     */
    @Query(nativeQuery = true, value =
        "SELECT r.id, r.name, r.price_range, COALESCE(CEIL(SUM(o.restaurant_rating) / NULLIF(COUNT(o.id), 0)), 0) AS rating " +
        "FROM restaurants r " +
        "LEFT JOIN orders o ON r.id = o.restaurant_id " +
        "WHERE r.id = :restaurantId " +
        "GROUP BY r.id")
    List<Object[]> findRestaurantWithAverageRatingById(@Param("restaurantId") int restaurantId);
    
    /**
     * Finds restaurants based on the provided rating and price range.
     *
     * Executes a native SQL query that retrieves restaurants with their information, including a calculated
     * average rating rounded up to the ceiling.
     *
     * @param rating     The minimum rounded-up average rating of the restaurants. (Optional)
     * @param priceRange The price range of the restaurants. (Optional)
     * @return A list of Object arrays representing the selected columns from the query result.
     *         Each Object array corresponds to a restaurant's information.
     *         An empty list is returned if no restaurant is found with the specified ID.
     */
    @Query(nativeQuery = true, value =
        "SELECT * FROM (" +
        "   SELECT r.id, r.name, r.price_range, COALESCE(CEIL(SUM(o.restaurant_rating) / NULLIF(COUNT(o.id), 0)), 0) AS rating " +
        "   FROM restaurants r " +
        "   LEFT JOIN orders o ON r.id = o.restaurant_id " +
        "   WHERE (:priceRange IS NULL OR r.price_range = :priceRange) " +
        "   GROUP BY r.id" +
        ") AS result " +
        "WHERE (:rating IS NULL OR result.rating = :rating)")
    List<Object[]> findRestaurantsByRatingAndPriceRange(@Param("rating") Integer rating, @Param("priceRange") Integer priceRange);

    // TODO
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value =
        "TODO Write SQL query here")
    void saveRestaurant(long userId, long addressId, String name, int priceRange, String phone, String email);

    // TODO
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value =
            "TODO Write SQL query here")
    void updateRestaurant(int restaurantId, String name, int priceRange, String phone);

    // TODO
    @Query(nativeQuery = true, value = "TODO Write SQL query here")
    Optional<Restaurant> findRestaurantById(@Param("restaurantId") int restaurantId);

    @Query(nativeQuery = true, value = "SELECT LAST_INSERT_ID() AS id")
    int getLastInsertedId();

    // TODO
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "TODO Write SQL query here")
    void deleteRestaurantById(@Param("restaurantId") int restaurantId);

}
