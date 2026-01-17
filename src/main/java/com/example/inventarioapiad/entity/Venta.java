package com.example.inventarioapiad.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

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
    private LocalDateTime fechaVenta;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El número de pedido no puede estar vacío")
    private String numeroPedido;

    @Column(nullable = false)
    private String estado = "PENDIENTE";  // PENDIENTE, ENVIADA, ENTREGADA, CANCELADA

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}