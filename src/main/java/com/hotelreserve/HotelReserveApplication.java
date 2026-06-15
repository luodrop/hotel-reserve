package com.hotelreserve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.hotelreserve.entity")
@EnableJpaRepositories("com.hotelreserve.repository")
public class HotelReserveApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelReserveApplication.class, args);
    }
}
