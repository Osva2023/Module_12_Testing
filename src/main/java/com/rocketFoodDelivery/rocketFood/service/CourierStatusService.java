package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.CourierStatus;
import com.rocketFoodDelivery.rocketFood.repository.CourierStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourierStatusService {

    CourierStatusRepository courierStatusRepository;
    
    @Autowired
    public CourierStatusService(CourierStatusRepository courierStatusRepository){
        this.courierStatusRepository = courierStatusRepository;
    }

    public CourierStatus findByName(String name) {
        return courierStatusRepository.findByName(name);
    }
}
