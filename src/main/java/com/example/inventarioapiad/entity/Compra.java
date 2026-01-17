package com.example.inventarioapiad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacen almacen;

    @Column(nullable = false)
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Column(nullable = false)
    @Positive(message = "El precio unitario debe ser mayor a 0")
    private Float precioUnitario;

    @Column(nullable = false)
    private LocalDateTime fechaCompra;

    private String numeroFactura;

    @Column(nullable = false)
    private String estado = "PENDIENTE";  // PENDIENTE, RECIBIDA, CANCELADA

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}