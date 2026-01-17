package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Venta;
import com.example.inventarioapiad.repository.VentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {

    private static final Logger logger = LoggerFactory.getLogger(VentaService.class);

    @Autowired
    private VentaRepository ventaRepository;

    public Venta crear(Venta venta) {
        logger.info("Creando venta con número de pedido: " + venta.getNumeroPedido());

        if (venta.getCliente() == null) {
            logger.error("Error: Cliente obligatorio");
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        if (venta.getProducto() == null) {
            logger.error("Error: Producto obligatorio");
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (venta.getAlmacen() == null) {
            logger.error("Error: Almacén obligatorio");
            throw new IllegalArgumentException("El almacén es obligatorio");
        }
        if (venta.getCantidad() == null || venta.getCantidad() <= 0) {
            logger.error("Error: Cantidad inválida");
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (venta.getPrecioUnitario() == null || venta.getPrecioUnitario() <= 0) {
            logger.error("Error: Precio unitario inválido");
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
        if (venta.getFechaVenta() == null) {
            logger.error("Error: Fecha de venta obligatoria");
            throw new IllegalArgumentException("La fecha de venta es obligatoria");
        }
        if (venta.getNumeroPedido() == null || venta.getNumeroPedido().isBlank()) {
            logger.error("Error: Número de pedido vacío");
            throw new IllegalArgumentException("El número de pedido es obligatorio");
        }

        Venta creada = ventaRepository.save(venta);
        logger.info("Venta creada exitosamente con ID: " + creada.getId());
        return creada;
    }

    public Venta buscarPorId(Long id) {
        logger.info("Buscando venta con ID: " + id);

        if (id == null || id <= 0) {
            logger.error("Error: ID inválido");
            throw new IllegalArgumentException("El ID debe ser válido");
        }

        return ventaRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Venta no encontrada con ID: " + id);
                    return new RuntimeException("Venta no encontrada con ID: " + id);
                });
    }

    public List<Venta> buscarTodos() {
        logger.info("Listando todas las ventas");
        return (List<Venta>) ventaRepository.findAll();
    }

    public Venta actualizar(Long id, Venta ventaActualizada) {
        logger.info("Actualizando venta con ID: " + id);

        Venta venta = buscarPorId(id);

        if (ventaActualizada.getCliente() != null) {
            venta.setCliente(ventaActualizada.getCliente());
        }
        if (ventaActualizada.getProducto() != null) {
            venta.setProducto(ventaActualizada.getProducto());
        }
        if (ventaActualizada.getAlmacen() != null) {
            venta.setAlmacen(ventaActualizada.getAlmacen());
        }
        if (ventaActualizada.getCantidad() != null && ventaActualizada.getCantidad() > 0) {
            venta.setCantidad(ventaActualizada.getCantidad());
        }
        if (ventaActualizada.getPrecioUnitario() != null && ventaActualizada.getPrecioUnitario() > 0) {
            venta.setPrecioUnitario(ventaActualizada.getPrecioUnitario());
        }
        if (ventaActualizada.getFechaVenta() != null) {
            venta.setFechaVenta(ventaActualizada.getFechaVenta());
        }
        if (ventaActualizada.getNumeroPedido() != null) {
            venta.setNumeroPedido(ventaActualizada.getNumeroPedido());
        }
        if (ventaActualizada.getEstado() != null) {
            venta.setEstado(ventaActualizada.getEstado());
        }

        Venta actualizada = ventaRepository.save(venta);
        logger.info("Venta actualizada exitosamente con ID: " + id);
        return actualizada;
    }

    public void eliminar(Long id) {
        logger.info("Eliminando venta con ID: " + id);

        Venta venta = buscarPorId(id);
        venta.setEstado("CANCELADA");
        ventaRepository.save(venta);

        logger.info("Venta cancelada (eliminada) con ID: " + id);
    }

    // FILTRADO: Buscar ventas con hasta 3 campos
    public List<Venta> buscarConFiltros(String estado, Integer cantidad, String numeroPedido) {
        logger.info("Filtrando ventas - estado: " + estado + ", cantidad: " + cantidad + ", numeroPedido: " + numeroPedido);

        List<Venta> ventas = new ArrayList<>((Collection) ventaRepository.findAll());

        if (estado != null && !estado.isBlank()) {
            ventas = ventas.stream()
                    .filter(v -> v.getEstado().toLowerCase().contains(estado.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (cantidad != null) {
            ventas = ventas.stream()
                    .filter(v -> v.getCantidad() >= cantidad)
                    .collect(Collectors.toList());
        }

        if (numeroPedido != null && !numeroPedido.isBlank()) {
            ventas = ventas.stream()
                    .filter(v -> v.getNumeroPedido().toLowerCase().contains(numeroPedido.toLowerCase()))
                    .collect(Collectors.toList());
        }

        logger.info("Filtrado completado. Resultados: " + ventas.size() + " ventas");
        return ventas;
    }
}