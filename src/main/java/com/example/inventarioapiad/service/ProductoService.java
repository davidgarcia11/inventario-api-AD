package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Producto;
import com.example.inventarioapiad.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    // CREATE
    public Producto crear(Producto producto) {
        logger.info("Creando producto: " + producto.getNombre());

        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            logger.error("Error: Nombre del producto vacío");
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (producto.getSku() == null || producto.getSku().isBlank()) {
            logger.error("Error: SKU vacío");
            throw new IllegalArgumentException("El SKU es obligatorio");
        }
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta() <= 0) {
            logger.error("Error: Precio de venta inválido");
            throw new IllegalArgumentException("El precio de venta debe ser mayor a 0");
        }
        if (producto.getStockTotal() == null || producto.getStockTotal() < 0) {
            logger.error("Error: Stock negativo");
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        Producto creado = productoRepository.save(producto);
        logger.info("Producto creado exitosamente con ID: " + creado.getId());
        return creado;
    }

    // READ
    public Producto buscarPorId(Long id) {
        logger.info("Buscando producto con ID: " + id);

        if (id == null || id <= 0) {
            logger.error("Error: ID inválido");
            throw new IllegalArgumentException("El ID debe ser válido");
        }

        return productoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Producto no encontrado con ID: " + id);
                    return new RuntimeException("Producto no encontrado con ID: " + id);
                });
    }

    // READ ALL
    public List<Producto> buscarTodos() {
        logger.info("Listando todos los productos");
        return (List<Producto>) productoRepository.findAll();
    }

    // UPDATE
    public Producto actualizar(Long id, Producto productoActualizado) {
        logger.info("Actualizando producto con ID: " + id);

        Producto producto = buscarPorId(id);

        if (productoActualizado.getNombre() != null && !productoActualizado.getNombre().isBlank()) {
            producto.setNombre(productoActualizado.getNombre());
        }
        if (productoActualizado.getSku() != null && !productoActualizado.getSku().isBlank()) {
            producto.setSku(productoActualizado.getSku());
        }
        if (productoActualizado.getDescripcion() != null) {
            producto.setDescripcion(productoActualizado.getDescripcion());
        }
        if (productoActualizado.getPrecioVenta() != null && productoActualizado.getPrecioVenta() > 0) {
            producto.setPrecioVenta(productoActualizado.getPrecioVenta());
        }
        if (productoActualizado.getPrecioCosto() != null && productoActualizado.getPrecioCosto() > 0) {
            producto.setPrecioCosto(productoActualizado.getPrecioCosto());
        }
        if (productoActualizado.getStockTotal() != null && productoActualizado.getStockTotal() >= 0) {
            producto.setStockTotal(productoActualizado.getStockTotal());
        }
        if (productoActualizado.getActivo() != null) {
            producto.setActivo(productoActualizado.getActivo());
        }

        Producto actualizado = productoRepository.save(producto);
        logger.info("Producto actualizado exitosamente con ID: " + id);
        return actualizado;
    }

    // DELETE
    public void eliminar(Long id) {
        logger.info("Eliminando producto con ID: " + id);

        Producto producto = buscarPorId(id);
        producto.setActivo(false);  // Soft delete: marcar como inactivo
        productoRepository.save(producto);

        logger.info("Producto eliminado (soft delete) con ID: " + id);
    }

    // FILTRADO: Buscar productos con hasta 3 campos
    public List<Producto> buscarConFiltros(String nombre, String sku, Float precioVenta) {
        logger.info("Filtrando productos - nombre: " + nombre + ", sku: " + sku + ", precioVenta: " + precioVenta);

        List<Producto> productos = ((List<Producto>) productoRepository.findAll()).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .collect(Collectors.toList());

        if (nombre != null && !nombre.isBlank()) {
            productos = productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (sku != null && !sku.isBlank()) {
            productos = productos.stream()
                    .filter(p -> p.getSku().toLowerCase().contains(sku.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (precioVenta != null) {
            productos = productos.stream()
                    .filter(p -> p.getPrecioVenta() >= precioVenta)
                    .collect(Collectors.toList());
        }

        logger.info("Filtrado completado. Resultados: " + productos.size() + " productos");
        return productos;
    }
}