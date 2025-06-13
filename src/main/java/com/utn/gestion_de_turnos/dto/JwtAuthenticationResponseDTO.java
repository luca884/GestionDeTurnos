package com.utn.gestion_de_turnos.dto;

import lombok.Data;

@Data
public class JwtAuthenticationResponseDTO {
    private String token;
    private String rol;

    public JwtAuthenticationResponseDTO(String token, String rol) {
        this.token = token;
        this.rol = rol;
    }
}
