package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class AddressService {
    private final AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository){
        this.addressRepository = addressRepository;
    }

    public Optional<Address> findById(int id){
        return addressRepository.findById(id);
    }

    public Optional<Integer> findLastAddressId() {
        List<Address> addresses = addressRepository.findAllByOrderByIdDesc();
        if (!addresses.isEmpty()) {
            return Optional.of(addresses.get(0).getId());
        } else {
            return Optional.empty();
        }
    }
    
    public Address saveAddress(Address address){
        return addressRepository.save(address);
    }

    @Transactional
    public int saveAddress(String streetAddress, String city, String postalCode) {
        try {
            addressRepository.saveAddress(streetAddress, city, postalCode);
            return addressRepository.getLastInsertedId();
        } catch (DataAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public void delete(int id) {
        addressRepository.deleteById(id);
    }
}