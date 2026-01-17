package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Proveedor;
import com.example.inventarioapiad.repository.ProveedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProveedorService {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorService.class);

    @Autowired
    private ProveedorRepository proveedorRepository;

    public Proveedor crear(Proveedor proveedor) {
        logger.info("Creando proveedor: " + proveedor.getNombre());

        if (proveedor.getNombre() == null || proveedor.getNombre().isBlank()) {
            logger.error("Error: Nombre del proveedor vacío");
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }
        if (proveedor.getEmail() == null || proveedor.getEmail().isBlank()) {
            logger.error("Error: Email del proveedor vacío");
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!proveedor.getEmail().contains("@")) {
            logger.error("Error: Email inválido");
            throw new IllegalArgumentException("El email debe ser válido");
        }

        Proveedor creado = proveedorRepository.save(proveedor);
        logger.info("Proveedor creado exitosamente con ID: " + creado.getId());
        return creado;
    }

    public Proveedor buscarPorId(Long id) {
        logger.info("Buscando proveedor con ID: " + id);

        if (id == null || id <= 0) {
            logger.error("Error: ID inválido");
            throw new IllegalArgumentException("El ID debe ser válido");
        }

        return proveedorRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Proveedor no encontrado con ID: " + id);
                    return new RuntimeException("Proveedor no encontrado con ID: " + id);
                });
    }

    public List<Proveedor> buscarTodos() {
        logger.info("Listando todos los proveedores");
        return (List<Proveedor>) proveedorRepository.findAll();
    }

    public Proveedor actualizar(Long id, Proveedor proveedorActualizado) {
        logger.info("Actualizando proveedor con ID: " + id);

        Proveedor proveedor = buscarPorId(id);

        if (proveedorActualizado.getNombre() != null && !proveedorActualizado.getNombre().isBlank()) {
            proveedor.setNombre(proveedorActualizado.getNombre());
        }
        if (proveedorActualizado.getEmail() != null && !proveedorActualizado.getEmail().isBlank()) {
            proveedor.setEmail(proveedorActualizado.getEmail());
        }
        if (proveedorActualizado.getTelefono() != null) {
            proveedor.setTelefono(proveedorActualizado.getTelefono());
        }
        if (proveedorActualizado.getPais() != null) {
            proveedor.setPais(proveedorActualizado.getPais());
        }
        if (proveedorActualizado.getDiasEntrega() != null && proveedorActualizado.getDiasEntrega() > 0) {
            proveedor.setDiasEntrega(proveedorActualizado.getDiasEntrega());
        }
        if (proveedorActualizado.getActivo() != null) {
            proveedor.setActivo(proveedorActualizado.getActivo());
        }

        Proveedor actualizado = proveedorRepository.save(proveedor);
        logger.info("Proveedor actualizado exitosamente con ID: " + id);
        return actualizado;
    }

    public void eliminar(Long id) {
        logger.info("Eliminando proveedor con ID: " + id);

        Proveedor proveedor = buscarPorId(id);
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);

        logger.info("Proveedor eliminado (soft delete) con ID: " + id);
    }

    // FILTRADO: Buscar proveedores con hasta 3 campos
    public List<Proveedor> buscarConFiltros(String nombre, String email, Integer diasEntrega) {
        logger.info("Filtrando proveedores - nombre: " + nombre + ", email: " + email + ", diasEntrega: " + diasEntrega);

        List<Proveedor> proveedores = ((List<Proveedor>) proveedorRepository.findAll()).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .collect(Collectors.toList());

        if (nombre != null && !nombre.isBlank()) {
            proveedores = proveedores.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (email != null && !email.isBlank()) {
            proveedores = proveedores.stream()
                    .filter(p -> p.getEmail().toLowerCase().contains(email.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (diasEntrega != null) {
            proveedores = proveedores.stream()
                    .filter(p -> p.getDiasEntrega() <= diasEntrega)
                    .collect(Collectors.toList());
        }

        logger.info("Filtrado completado. Resultados: " + proveedores.size() + " proveedores");
        return proveedores;
    }
}