package com.utn.gestion_de_turnos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@NoArgsConstructor
@Entity
@Table(name = "admins")
public class Admin extends Usuario {


}
// SQL script para crear un admin por defecto (primero admin)
// INSERT INTO usuarios (nombre, apellido, dni, telefono, email, contrasena, rol)
//VALUES ('AdminNombre', 'AdminApellido', '12345678', '123456789', 'admin@example.com', '$2a$10$9TEbqoVJmsV2UUplyaw0X.tGUuS5igRSBOAkNkT29d/FkNnDnVJG2', 'ADMIN');
// contrasena para admin: admin123
// INSERT INTO admins (id)
// VALUES ((SELECT id FROM usuarios WHERE email = 'admin@example.com'));