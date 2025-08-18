package com.dem.Inventory;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class InventoryManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryManagementSystemApplication.class, args);


	}

	@PostConstruct
	public void printDbUrl() {
		System.out.println(">>> MYSQL_URL = " + System.getenv("MYSQL_URL"));
		System.out.println(">>> MYSQLHOST = " + System.getenv("MYSQLHOST"));
	}

}
