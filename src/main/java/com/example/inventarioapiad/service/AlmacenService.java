package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.repository.AlmacenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlmacenService {

    @Autowired
    private AlmacenRepository almacenRepository;

    public Almacen crear(Almacen almacen) {
        if (almacen.getNombre() == null || almacen.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del almacén es obligatorio");
        }
        if (almacen.getUbicacion() == null || almacen.getUbicacion().isBlank()) {
            throw new IllegalArgumentException("La ubicación es obligatoria");
        }
        if (almacen.getCapacidadMaxima() != null && almacen.getCapacidadMaxima() <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a 0");
        }
        return almacenRepository.save(almacen);
    }

    public Almacen buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido");
        }
        return almacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + id));
    }

    public List<Almacen> buscarTodos() {
        return (List<Almacen>) almacenRepository.findAll();
    }

    public Almacen actualizar(Long id, Almacen almacenActualizado) {
        Almacen almacen = buscarPorId(id);

        if (almacenActualizado.getNombre() != null && !almacenActualizado.getNombre().isBlank()) {
            almacen.setNombre(almacenActualizado.getNombre());
        }
        if (almacenActualizado.getUbicacion() != null && !almacenActualizado.getUbicacion().isBlank()) {
            almacen.setUbicacion(almacenActualizado.getUbicacion());
        }
        if (almacenActualizado.getCapacidadMaxima() != null && almacenActualizado.getCapacidadMaxima() > 0) {
            almacen.setCapacidadMaxima(almacenActualizado.getCapacidadMaxima());
        }
        if (almacenActualizado.getStockActual() != null && almacenActualizado.getStockActual() >= 0) {
            almacen.setStockActual(almacenActualizado.getStockActual());
        }
        if (almacenActualizado.getResponsable() != null) {
            almacen.setResponsable(almacenActualizado.getResponsable());
        }
        if (almacenActualizado.getActivo() != null) {
            almacen.setActivo(almacenActualizado.getActivo());
        }

        return almacenRepository.save(almacen);
    }

    public void eliminar(Long id) {
        Almacen almacen = buscarPorId(id);
        almacen.setActivo(false);
        almacenRepository.save(almacen);
    }

    // FILTRADO: Buscar almacenes con hasta 3 campos
    public List<Almacen> buscarConFiltros(String nombre, String ubicacion, Integer capacidadMaxima) {
        List<Almacen> almacenes = ((List<Almacen>) almacenRepository.findAll()).stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .collect(Collectors.toList());

        if (nombre != null && !nombre.isBlank()) {
            almacenes = almacenes.stream()
                    .filter(a -> a.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (ubicacion != null && !ubicacion.isBlank()) {
            almacenes = almacenes.stream()
                    .filter(a -> a.getUbicacion().toLowerCase().contains(ubicacion.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (capacidadMaxima != null) {
            almacenes = almacenes.stream()
                    .filter(a -> a.getCapacidadMaxima() >= capacidadMaxima)
                    .collect(Collectors.toList());
        }

        return almacenes;
    }
}