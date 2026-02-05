package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Compra;
import com.example.inventarioapiad.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    public Compra crear(Compra compra) {
        log.info("Creando compra con número de factura: " + compra.getNumeroFactura());

        if (compra.getProveedor() == null) {
            log.error("Error: Proveedor obligatorio");
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
        if (compra.getProducto() == null) {
            log.error("Error: Producto obligatorio");
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (compra.getAlmacen() == null) {
            log.error("Error: Almacén obligatorio");
            throw new IllegalArgumentException("El almacén es obligatorio");
        }
        if (compra.getCantidad() == null || compra.getCantidad() <= 0) {
            log.error("Error: Cantidad inválida");
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (compra.getPrecioUnitario() == null || compra.getPrecioUnitario() <= 0) {
            log.error("Error: Precio unitario inválido");
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
        if (compra.getFechaCompra() == null) {
            log.error("Error: Fecha de compra obligatoria");
            throw new IllegalArgumentException("La fecha de compra es obligatoria");
        }

        Compra creada = compraRepository.save(compra);
        log.info("Compra creada exitosamente con ID: " + creada.getId());
        return creada;
    }

    public Compra buscarPorId(Long id) {
        log.info("Buscando compra con ID: " + id);

        if (id == null || id <= 0) {
            log.error("Error: ID inválido");
            throw new IllegalArgumentException("El ID debe ser válido");
        }

        return compraRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Compra no encontrada con ID: " + id);
                    return new RuntimeException("Compra no encontrada con ID: " + id);
                });
    }

    public List<Compra> buscarTodos() {
        log.info("Listando todas las compras");
        return (List<Compra>) compraRepository.findAll();
    }

    public Compra actualizar(Long id, Compra compraActualizada) {
        log.info("Actualizando compra con ID: " + id);

        Compra compra = buscarPorId(id);

        if (compraActualizada.getProveedor() != null) {
            compra.setProveedor(compraActualizada.getProveedor());
        }
        if (compraActualizada.getProducto() != null) {
            compra.setProducto(compraActualizada.getProducto());
        }
        if (compraActualizada.getAlmacen() != null) {
            compra.setAlmacen(compraActualizada.getAlmacen());
        }
        if (compraActualizada.getCantidad() != null && compraActualizada.getCantidad() > 0) {
            compra.setCantidad(compraActualizada.getCantidad());
        }
        if (compraActualizada.getPrecioUnitario() != null && compraActualizada.getPrecioUnitario() > 0) {
            compra.setPrecioUnitario(compraActualizada.getPrecioUnitario());
        }
        if (compraActualizada.getFechaCompra() != null) {
            compra.setFechaCompra(compraActualizada.getFechaCompra());
        }
        if (compraActualizada.getNumeroFactura() != null) {
            compra.setNumeroFactura(compraActualizada.getNumeroFactura());
        }
        if (compraActualizada.getEstado() != null) {
            compra.setEstado(compraActualizada.getEstado());
        }

        Compra actualizada = compraRepository.save(compra);
        log.info("Compra actualizada exitosamente con ID: " + id);
        return actualizada;
    }

    public void eliminar(Long id) {
        log.info("Eliminando compra con ID: " + id);

        Compra compra = buscarPorId(id);
        compra.setEstado("CANCELADA");
        compraRepository.save(compra);

        log.info("Compra cancelada (eliminada) con ID: " + id);
    }

    // FILTRADO: Buscar compras con hasta 3 campos
    public List<Compra> buscarConFiltros(String estado, Integer cantidad, String numeroFactura) {
        log.info("Filtrando compras - estado: " + estado + ", cantidad: " + cantidad + ", numeroFactura: " + numeroFactura);

        List<Compra> compras = new ArrayList<>((Collection) compraRepository.findAll());

        if (estado != null && !estado.isBlank()) {
            compras = compras.stream()
                    .filter(c -> c.getEstado().toLowerCase().contains(estado.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (cantidad != null) {
            compras = compras.stream()
                    .filter(c -> c.getCantidad().equals(cantidad))
                    .collect(Collectors.toList());
        }

        if (numeroFactura != null && !numeroFactura.isBlank()) {
            compras = compras.stream()
                    .filter(c -> c.getNumeroFactura().toLowerCase().contains(numeroFactura.toLowerCase()))
                    .collect(Collectors.toList());
        }

        log.info("Filtrado completado. Resultados: " + compras.size() + " compras");
        return compras;
    }
}