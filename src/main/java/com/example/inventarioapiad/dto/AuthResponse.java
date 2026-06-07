package com.example.inventarioapiad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta que devolvemos al cliente cuando se autentica con éxito.
// expiresIn está en segundos para que Postman pueda calcular cuándo
// renovar el token automáticamente en el pre-request script.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String username;
    private long expiresIn;
}
