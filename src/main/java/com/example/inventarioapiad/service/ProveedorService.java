package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Proveedor;
import com.example.inventarioapiad.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public Proveedor crear(Proveedor proveedor) {
        if (proveedor.getNombre() == null || proveedor.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }
        if (proveedor.getEmail() == null || proveedor.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!proveedor.getEmail().contains("@")) {
            throw new IllegalArgumentException("El email debe ser válido");
        }
        return proveedorRepository.save(proveedor);
    }

    public Proveedor buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido");
        }
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
    }

    public List<Proveedor> buscarTodos() {
        return (List<Proveedor>) proveedorRepository.findAll();
    }

    public Proveedor actualizar(Long id, Proveedor proveedorActualizado) {
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

        return proveedorRepository.save(proveedor);
    }

    public void eliminar(Long id) {
        Proveedor proveedor = buscarPorId(id);
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    // FILTRADO: Buscar proveedores con hasta 3 campos
    public List<Proveedor> buscarConFiltros(String nombre, String email, Integer diasEntrega) {
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

        return proveedores;
    }
}