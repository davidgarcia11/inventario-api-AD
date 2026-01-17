package com.example.inventarioapiad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre del proveedor no puede estar vacío")
    private String nombre;

    @Column(nullable = false)
    @Email(message = "El email debe ser válido")
    private String email;

    private String telefono;

    private String pais;

    @Positive(message = "Los días de entrega deben ser mayor a 0")
    private Integer diasEntrega;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}