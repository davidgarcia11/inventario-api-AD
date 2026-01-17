package com.example.inventarioapiad.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El SKU no puede estar vacío")
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Positive(message = "El precio de costo debe ser mayor a 0")
    private Float precioCosto;

    @Column(nullable = false)
    @Positive(message = "El precio de venta debe ser mayor a 0")
    private Float precioVenta;

    @Column(nullable = false)
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stockTotal;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
