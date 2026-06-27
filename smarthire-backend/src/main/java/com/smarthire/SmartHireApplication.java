package com.smarthire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartHire AI — Main Application Entry Point.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 *   - @Configuration: Marks this class as a source of bean definitions
 *   - @EnableAutoConfiguration: Tells Spring Boot to auto-configure based on dependencies
 *   - @ComponentScan: Scans the com.smarthire package and all sub-packages for Spring components
 *
 * When this class runs, Spring Boot:
 *   1. Starts an embedded Tomcat server
 *   2. Connects to the MySQL database
 *   3. Registers all controllers, services, and repositories
 *   4. Applies security configurations
 *   5. Makes the application ready to accept HTTP requests
 */
@SpringBootApplication
public class SmartHireApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHireApplication.class, args);
    }
}
