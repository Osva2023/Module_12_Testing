package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.Employee;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.EmployeeRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    UserRepository userRepository;
    EmployeeRepository employeeRepository;

    @Autowired
    public UserService(UserRepository userRepository , EmployeeRepository employeeRepository){
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    public void populateEmployeeStatus(UserEntity userEntity) {
        Optional<Employee> employee = employeeRepository.findByUserEntityId(userEntity.getId());
        userEntity.setEmployee(employee.isPresent());
    }

    public List<UserEntity> findAllUsers(){
        return userRepository.findAll();
    }
    
    public Optional<UserEntity> findById(int id){
        return userRepository.findById(id);
    }

    public Optional<Integer> findLastUserId() {
        List<UserEntity> Users = userRepository.findAllByOrderByIdDesc();
        if (!Users.isEmpty()) {
            return Optional.of(Users.get(0).getId());
        } else {
            return Optional.empty();
        }
    }

    public UserEntity saveUser(UserEntity user){
        return userRepository.save(user);
    }
    
    public void delete(int id) {
        userRepository.deleteById(id);
    }


}