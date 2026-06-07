package com.example.inventarioapiad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO con los datos que envía el cliente para autenticarse.
@Data
public class LoginRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
