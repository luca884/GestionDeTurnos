package com.utn.gestion_de_turnos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@NoArgsConstructor
@Entity
@Table(name = "admins")
public class Admin extends Usuario {


}
