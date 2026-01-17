package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Producto;
import com.example.inventarioapiad.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // CREATE
    public Producto crear(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (producto.getSku() == null || producto.getSku().isBlank()) {
            throw new IllegalArgumentException("El SKU es obligatorio");
        }
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta() <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor a 0");
        }
        if (producto.getStockTotal() == null || producto.getStockTotal() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        return productoRepository.save(producto);
    }

    // READ
    public Producto buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido");
        }
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    // READ ALL
    public List<Producto> buscarTodos() {
        return (List<Producto>) productoRepository.findAll();
    }

    // UPDATE
    public Producto actualizar(Long id, Producto productoActualizado) {
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

        return productoRepository.save(producto);
    }

    // DELETE
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        producto.setActivo(false);  // Soft delete: marcar como inactivo
        productoRepository.save(producto);
    }

    // FILTRADO: Buscar productos con hasta 3 campos
    public List<Producto> buscarConFiltros(String nombre, String sku, Float precioVenta) {
        List<Producto> productos = new ArrayList<>((Collection) productoRepository.findAll());

        // Filtrar por nombre
        if (nombre != null && !nombre.isEmpty()) {
            productos = productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filtrar por SKU
        if (sku != null && !sku.isEmpty()) {
            productos = productos.stream()
                    .filter(p -> p.getSku().toLowerCase().contains(sku.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filtrar por precio de venta
        if (precioVenta != null) {
            productos = productos.stream()
                    .filter(p -> p.getPrecioVenta() >= precioVenta)
                    .collect(Collectors.toList());
        }

        return productos;
    }
}