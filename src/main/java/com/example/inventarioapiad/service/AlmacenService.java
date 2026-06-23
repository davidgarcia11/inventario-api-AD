package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.repository.AlmacenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlmacenService {

    @Autowired
    private AlmacenRepository almacenRepository;

    public Almacen crear(Almacen almacen) {
        log.info("Creando almacén: " + almacen.getNombre());

        if (almacen.getNombre() == null || almacen.getNombre().isBlank()) {
            log.error("Error: Nombre del almacén vacío");
            throw new IllegalArgumentException("El nombre del almacén es obligatorio");
        }
        if (almacen.getUbicacion() == null || almacen.getUbicacion().isBlank()) {
            log.error("Error: Ubicación vacía");
            throw new IllegalArgumentException("La ubicación es obligatoria");
        }
        if (almacen.getCapacidadMaxima() != null && almacen.getCapacidadMaxima() <= 0) {
            log.error("Error: Capacidad máxima inválida");
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a 0");
        }

        Almacen creado = almacenRepository.save(almacen);
        log.info("Almacén creado exitosamente con ID: " + creado.getId());
        return creado;
    }

    public Almacen buscarPorId(Long id) {
        log.info("Buscando almacén con ID: " + id);

        if (id == null || id <= 0) {
            log.error("Error: ID inválido");
            throw new IllegalArgumentException("El ID debe ser válido");
        }

        return almacenRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Almacén no encontrado con ID: " + id);
                    return new RuntimeException("Almacén no encontrado con ID: " + id);
                });
    }

    public List<Almacen> buscarTodos() {
        log.info("Listando todos los almacenes");
        return (List<Almacen>) almacenRepository.findAll();
    }

    public Almacen actualizar(Long id, Almacen almacenActualizado) {
        log.info("Actualizando almacén con ID: " + id);

        Almacen almacen = buscarPorId(id);

        if (almacenActualizado.getNombre() != null && !almacenActualizado.getNombre().isBlank()) {
            almacen.setNombre(almacenActualizado.getNombre());
        }
        if (almacenActualizado.getUbicacion() != null && !almacenActualizado.getUbicacion().isBlank()) {
            almacen.setUbicacion(almacenActualizado.getUbicacion());
        }
        // Si el cliente envía estos campos, los validamos y rechazamos los
        // valores fuera de rango. Antes se ignoraban en silencio: un PUT con
        // capacidadMaxima=0 respondía 200 sin cambiar nada, confundiendo al
        // cliente. Misma corrección que se aplicó a ProductoService.
        if (almacenActualizado.getCapacidadMaxima() != null) {
            if (almacenActualizado.getCapacidadMaxima() <= 0) {
                log.error("Error: Capacidad máxima inválida");
                throw new IllegalArgumentException("La capacidad máxima debe ser mayor a 0");
            }
            almacen.setCapacidadMaxima(almacenActualizado.getCapacidadMaxima());
        }
        if (almacenActualizado.getStockActual() != null) {
            if (almacenActualizado.getStockActual() < 0) {
                log.error("Error: Stock actual negativo");
                throw new IllegalArgumentException("El stock actual no puede ser negativo");
            }
            almacen.setStockActual(almacenActualizado.getStockActual());
        }
        if (almacenActualizado.getResponsable() != null) {
            almacen.setResponsable(almacenActualizado.getResponsable());
        }
        if (almacenActualizado.getActivo() != null) {
            almacen.setActivo(almacenActualizado.getActivo());
        }

        Almacen actualizado = almacenRepository.save(almacen);
        log.info("Almacén actualizado exitosamente con ID: " + id);
        return actualizado;
    }

    public void eliminar(Long id) {
        log.info("Eliminando almacén con ID: " + id);

        Almacen almacen = buscarPorId(id);
        almacen.setActivo(false);
        almacenRepository.save(almacen);

        log.info("Almacén eliminado (soft delete) con ID: " + id);
    }

    // -------------------------------------------------------------------
    // Métodos extra que usa la V2 (filtros con prioritario y borrado
    // condicional por el campo "prioritario").
    // -------------------------------------------------------------------

    // Igual que buscarConFiltros pero acepta un 4º filtro opcional por el
    // campo "prioritario", que es el que introduce la V2. Si prioritario
    // es null, no filtra por él.
    public List<Almacen> buscarConFiltrosV2(String nombre, String ubicacion,
                                            Integer capacidadMaxima, Boolean prioritario) {
        List<Almacen> resultado = buscarConFiltros(nombre, ubicacion, capacidadMaxima);
        if (prioritario != null) {
            resultado = resultado.stream()
                    .filter(a -> prioritario.equals(a.getPrioritario()))
                    .collect(Collectors.toList());
        }
        return resultado;
    }

    // Borrado especial usado por la V2: si el almacén está marcado como
    // prioritario, se rechaza con una IllegalStateException (el controller
    // la mapea a un 409 Conflict). Si no es prioritario, se hace soft
    // delete como en la V1.
    public void eliminarSiNoPrioritario(Long id) {
        log.info("Intentando eliminar almacén V2 con ID: " + id);

        Almacen almacen = buscarPorId(id);
        if (Boolean.TRUE.equals(almacen.getPrioritario())) {
            log.error("Bloqueo: el almacén ID " + id + " es prioritario, no se puede eliminar");
            throw new IllegalStateException(
                    "No se puede eliminar un almacén prioritario. Marca prioritario=false antes de borrar.");
        }

        almacen.setActivo(false);
        almacenRepository.save(almacen);
        log.info("Almacén V2 eliminado (soft delete) con ID: " + id);
    }

    // FILTRADO: Buscar almacenes con hasta 3 campos
    public List<Almacen> buscarConFiltros(String nombre, String ubicacion, Integer capacidadMaxima) {
        log.info("Filtrando almacenes - nombre: " + nombre + ", ubicacion: " + ubicacion + ", capacidadMaxima: " + capacidadMaxima);

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
                    .filter(a -> a.getCapacidadMaxima().equals(capacidadMaxima))
                    .collect(Collectors.toList());
        }

        log.info("Filtrado completado. Resultados: " + almacenes.size() + " almacenes");
        return almacenes;
    }
}