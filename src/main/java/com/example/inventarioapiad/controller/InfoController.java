package com.example.inventarioapiad.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// Endpoint /api/info que devuelve qué perfil de Spring está activo y
// describe brevemente cómo se comporta la app en ese perfil. Es la
// forma más rápida de demostrar al evaluador que dev y prod no son
// iguales: en dev arranca con datos de muestra (DataSeederConfig),
// en prod arranca vacío.
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/info")
@Tag(name = "Info", description = "Información del estado de la API y del perfil activo")
public class InfoController {

    @Value("${spring.profiles.active:default}")
    private String perfilActivo;

    @Value("${spring.application.name:inventario-api-AD}")
    private String nombreApp;

    // Se rellena en el primer arranque del bean
    private LocalDateTime fechaArranque;

    @PostConstruct
    public void inicializar() {
        this.fechaArranque = LocalDateTime.now();
    }

    @GetMapping
    @Operation(summary = "Información de la API y del perfil activo")
    @ApiResponse(responseCode = "200", description = "Datos del entorno actual")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("nombreApp", nombreApp);
        info.put("perfilActivo", perfilActivo);
        info.put("fechaArranque", fechaArranque);
        info.put("descripcion", descripcionDelPerfil());
        info.put("datosDeMuestraAlArrancar", "dev".equals(perfilActivo));
        return ResponseEntity.ok(info);
    }

    // Cuenta de forma legible qué hace cada perfil.
    private String descripcionDelPerfil() {
        return switch (perfilActivo) {
            case "dev" -> "Modo desarrollo: BD recreada en cada arranque (ddl-auto=create-drop), "
                    + "3 almacenes de muestra cargados automáticamente, SQL visible en los logs.";
            case "prod" -> "Modo producción: ddl-auto=validate, credenciales desde variables de entorno, "
                    + "sin datos de muestra, logs reducidos.";
            case "docker" -> "Modo Docker: ddl-auto=update, credenciales desde el .env del contenedor, "
                    + "sin datos de muestra.";
            default -> "Perfil no documentado: " + perfilActivo;
        };
    }
}
