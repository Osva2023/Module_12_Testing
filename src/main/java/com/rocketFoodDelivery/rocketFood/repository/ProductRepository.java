package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findById(int id);
    List <Product> findAll();
    List<Product> findByRestaurantId(int restaurantId);

    // TODO
    @Query(nativeQuery = true, value = "TODO Write SQL query here")
    List<Product> findProductsByRestaurantId(@Param("restaurantId") int restaurantId);

    // TODO
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "TODO Write SQL query here")
    void deleteProductsByRestaurantId(@Param("restaurantId") int restaurantId);
}
