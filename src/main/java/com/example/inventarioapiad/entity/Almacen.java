package com.example.inventarioapiad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "almacenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Almacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre del almacén no puede estar vacío")
    private String nombre;

    @Column(nullable = false)
    @NotBlank(message = "La ubicación no puede estar vacía")
    private String ubicacion;

    @Positive(message = "La capacidad máxima debe ser mayor a 0")
    private Integer capacidadMaxima;

    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual = 0;

    private String responsable;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}