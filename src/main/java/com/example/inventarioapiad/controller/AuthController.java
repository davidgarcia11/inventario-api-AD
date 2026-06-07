package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.dto.AuthResponse;
import com.example.inventarioapiad.dto.LoginRequest;
import com.example.inventarioapiad.dto.RegisterRequest;
import com.example.inventarioapiad.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controlador con los dos endpoints de autenticación:
//   POST /api/auth/register  - crear usuario
//   POST /api/auth/login     - obtener token JWT
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario",
               description = "Crea un usuario nuevo y devuelve un token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario duplicado")
    })
    public ResponseEntity<?> registrar(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse respuesta = usuarioService.registrar(request);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al registrar: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica al usuario y devuelve un token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse respuesta = usuarioService.autenticar(request);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            // Usuario o contraseña incorrectos: 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse(401, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error en el login: " + e.getMessage()));
        }
    }

    // Mismo formato de error que usan los demás controllers del proyecto
    public static class ErrorResponse {
        public int codigo;
        public String mensaje;

        public ErrorResponse(int codigo, String mensaje) {
            this.codigo = codigo;
            this.mensaje = mensaje;
        }

        public int getCodigo() { return codigo; }
        public String getMensaje() { return mensaje; }
    }
}
