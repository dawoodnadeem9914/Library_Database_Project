package com.unimas.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CSC3402 / CCS3402 - Enterprise Library Management System
 * Spring Boot 3 + Oracle + Spring Security + Thymeleaf + Bootstrap 5
 */
@SpringBootApplication
@EnableScheduling
public class LibraryProApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryProApplication.class, args);
    }
}
