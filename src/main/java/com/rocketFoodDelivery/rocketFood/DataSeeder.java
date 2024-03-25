package com.rocketFoodDelivery.rocketFood;

import com.github.javafaker.Faker;
import com.rocketFoodDelivery.rocketFood.models.*;
import com.rocketFoodDelivery.rocketFood.repository.*;
import com.rocketFoodDelivery.rocketFood.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataSeeder {

   private final UserRepository userRepository;
   private final RestaurantRepository restaurantRepository;
   private final ProductOrderRepository productOrderRepository;
   private final ProductRepository productRepository;
   private final OrderStatusRepository orderStatusRepository;
   private final OrderRepository orderRepository;
   private final EmployeeRepository employeeRepository;
   private final CustomerRepository customerRepository;
   private final AddressRepository addressRepository;
   private final CourierStatusRepository courierStatusRepository;
   private final CourierRepository courierRepository;
   UserService userService;

   @Autowired
   public DataSeeder(UserRepository userRepository,
                     RestaurantRepository restaurantRepository,
                     ProductOrderRepository productOrderRepository,
                     ProductRepository productRepository,
                     OrderStatusRepository orderStatusRepository,
                     OrderRepository orderRepository,
                     EmployeeRepository employeeRepository,
                     CustomerRepository customerRepository,
                     AddressRepository addressRepository, CourierStatusRepository courierStatusRepository, CourierRepository courierRepository) {
       this.userRepository = userRepository;
       this.restaurantRepository = restaurantRepository;
       this.productOrderRepository = productOrderRepository;
       this.productRepository = productRepository;
       this.orderStatusRepository = orderStatusRepository;
       this.orderRepository = orderRepository;
       this.employeeRepository = employeeRepository;
       this.customerRepository = customerRepository;
       this.addressRepository = addressRepository;
       this.courierStatusRepository = courierStatusRepository;
       this.courierRepository = courierRepository;
   }
   Faker faker = new Faker();
   @PostConstruct
   public void seedData() {

       seedUsers();
       seedAddresses();
       seedRestaurants();
       seedOrderStatuses();
       seedEmployees();
       seedCustomers();
       seedProducts();
       seedOrdersAndProductOrders();
       seedCourierStatuses();
       SeedCourier();
   }


   private void seedUsers() {
       List<UserEntity> users = new ArrayList<>();
       for (int i = 0; i < 20; i++) {
           UserEntity user = UserEntity.builder()
                   .name(faker.name().name())
                   .email(faker.internet().emailAddress())
                   .password("password" + i)
                   .build();
           users.add(user);
       }
       userRepository.saveAll(users);
   }
   private void seedAddresses() {
       List<Address> addresses = new ArrayList<>();
       for (int i = 0; i < 20; i++) {
           Address address = Address.builder()
                   .streetAddress(faker.address().streetAddress())
                   .city(faker.address().city())
                   .postalCode(faker.address().zipCode())
                   .build();
           addresses.add(address);
       }
       addressRepository.saveAll(addresses);
   }

   private void seedRestaurants() {
       List<Restaurant> restaurants = new ArrayList<>();
       List<UserEntity> users = userRepository.findAll();
       List<Address> addresses = addressRepository.findAll();
       for (int i = 0; i < 8; i++) {
           Restaurant restaurant = Restaurant.builder()
                   .userEntity(users.get(i+1))
                   .address(addresses.get(i +1))
                   .name(faker.company().name())
                   .phone("+1 437 - 221 - 698" +  i )
                   .email(faker.internet().emailAddress())
                   .priceRange(ThreadLocalRandom.current().nextInt(3)+1 )
                   .build();
           restaurants.add(restaurant);
       }
       restaurantRepository.saveAll(restaurants);
   }

   private void seedProducts() {
       List<Product> products = new ArrayList<>();
       List<Restaurant> restaurants = restaurantRepository.findAll();
       Random random = new Random();
       for (int i = 0; i < restaurants.size(); i++) {
           int numProducts = random.nextInt(3) + 4; // Generate 4 to 6 products
           for (int j = 0 ; j < numProducts ; j++){
               Product product = Product.builder()
                       .restaurant(restaurants.get(i))
                       .name(faker.food().dish())
                       .description("Description for Product " + i)
                       .cost(i * 100)
                       .build();
               products.add(product);
           }
       }
       productRepository.saveAll(products);
   }


   private void seedOrderStatuses() {
       List <String> order_statuses = Arrays.asList("pending", "in progress", "delivered");
       List<OrderStatus> orderStatuses = new ArrayList<>();
       for (int i = 0; i < 3; i++) {
           OrderStatus orderStatus = OrderStatus.builder()
                   .name(order_statuses.get(i))
                   .build();
           orderStatuses.add(orderStatus);
       }
       orderStatusRepository.saveAll(orderStatuses);
   }

   private void seedOrdersAndProductOrders() {
       Random random = new Random();
       List<Order> orders = new ArrayList<>();
       List<Restaurant> restaurants = restaurantRepository.findAll();
       List<Customer> customers = customerRepository.findAll();
       List<OrderStatus> orderStatuses = orderStatusRepository.findAll();
       for (int i = 0; i < 50; i++) {
           Restaurant restaurant = restaurants.get(ThreadLocalRandom.current().nextInt(restaurants.size()));
           Customer customer = customers.get(ThreadLocalRandom.current().nextInt(customers.size()));
           OrderStatus orderStatus = orderStatuses.get(ThreadLocalRandom.current().nextInt(orderStatuses.size()));
           Order order = Order.builder()
                   .restaurant(restaurant)
                   .customer(customer)
                   .order_status(orderStatus)
                   .restaurant_rating(ThreadLocalRandom.current().nextInt(5)+1 )
                   .build();
           orders.add(order);
           orderRepository.save(order);
           List <Product> products = productRepository.findByRestaurantId(restaurant.getId());
           for (int j = 1; j < products.size(); j++) {
               boolean continueLoop = true;
               while (continueLoop){
                   try {
                       Product product = products.get(ThreadLocalRandom.current().nextInt(products.size()));
                       ProductOrder productOrder = ProductOrder.builder()
                               .product(product)
                               .order(order)
                               .product_quantity(random.nextInt(3) + 1)
                               .product_unit_cost(product.getCost())
                               .build();
                       productOrderRepository.save(productOrder);
                       continueLoop = false;
                   } catch (Exception ignored) {
                   }
               }
           }
       }
    }




   private void seedEmployees() {
       List < Address > addresses = addressRepository.findAll();
       UserEntity erica = new UserEntity();
       erica.setEmail("erica.ger@gmail.com");
       erica.setName("Erica Ger");
       erica.setPassword("password");
       userRepository.save(erica);

       Address ericaAddress = new Address();
       ericaAddress.setStreetAddress("123 CodeBoxx Boulevard");
       ericaAddress.setCity("Montreal");
       ericaAddress.setPostalCode("H4G52Z");
       addressRepository.save(ericaAddress);

       Employee ericaEmployee = new Employee();
       ericaEmployee.setUserEntity(erica);
       ericaEmployee.setAddress(ericaAddress);
       ericaEmployee.setEmail(faker.internet().emailAddress());
       ericaEmployee.setPhone("000000");
       employeeRepository.save(ericaEmployee);

       List<Employee> employees = new ArrayList<>();
       List<UserEntity> users = userRepository.findAll();
       for (int i = 0; i < 2; i++) {
           Employee employee = Employee.builder()
                   .userEntity(users.get(i))

                   .phone("+1 437 - 2"+ i +"1 - 698" +  i )
                   .email(faker.internet().emailAddress())
                   .address(addresses.get(i + 1 ))
                   .build();
           employees.add(employee);
       }
       employeeRepository.saveAll(employees);
   }

   private void seedCustomers() {
       List<Customer> customers = new ArrayList<>();
       List<UserEntity> users = userRepository.findAll();
       List<Address> addresses = addressRepository.findAll();

       for (int i = 0; i < 10; i++) {
           Customer customer = Customer.builder()
                   .userEntity(users.get(i + 1))
                   .address(addresses.get(ThreadLocalRandom.current().nextInt(addresses.size() )))
                   .email(faker.internet().emailAddress())
                   .phone("+1 43"+ i +" - 221 - 698" +  i )
                   .build();
           customers.add(customer);
       }
       customerRepository.saveAll(customers);
   }
   private void seedCourierStatuses(){
       List<CourierStatus> courierStatuses= new ArrayList<>();
       List<String> Statuses = Arrays.asList("free", "busy", "full", "offline");
       for (String status : Statuses){
           CourierStatus courierStatus = CourierStatus.builder()
                   .name(status)
                   .build();
           courierStatuses.add(courierStatus);
       }
       courierStatusRepository.saveAll(courierStatuses);
   }
   private void SeedCourier(){
       List<Courier> couriers = new ArrayList<>();
       List<CourierStatus>courierStatuses = courierStatusRepository.findAll();
       List <Address> addresses = addressRepository.findAll();
       List<UserEntity> user = userRepository.findAll();
       for(int i = 0 ; i < 8 ; i++){
           Random random = new Random();
           Courier courier = Courier.builder()
                   .userEntity(user.get(i))
                   .address(addresses.get(i))
                   .email(faker.internet().emailAddress())
                   .phone("+1 4"+ i +"7 - 221 - 698" +  i )
                   .courierStatus(courierStatuses.get(random.nextInt(4)))
                   .build();
           couriers.add(courier);
       }
       courierRepository.saveAll(couriers);
   }
}
