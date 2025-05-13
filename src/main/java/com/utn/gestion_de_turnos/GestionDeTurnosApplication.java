package com.utn.gestion_de_turnos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class GestionDeTurnosApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionDeTurnosApplication.class, args);
		//admin123: $2a$10$9TEbqoVJmsV2UUplyaw0X.tGUuS5igRSBOAkNkT29d/FkNnDnVJG2

	}
}
