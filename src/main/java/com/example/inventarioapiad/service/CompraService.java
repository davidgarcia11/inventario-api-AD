package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Compra;
import com.example.inventarioapiad.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    public Compra crear(Compra compra) {
        if (compra.getProveedor() == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
        if (compra.getProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (compra.getAlmacen() == null) {
            throw new IllegalArgumentException("El almacén es obligatorio");
        }
        if (compra.getCantidad() == null || compra.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (compra.getPrecioUnitario() == null || compra.getPrecioUnitario() <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
        if (compra.getFechaCompra() == null) {
            throw new IllegalArgumentException("La fecha de compra es obligatoria");
        }
        return compraRepository.save(compra);
    }

    public Compra buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido");
        }
        return compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));
    }

    public List<Compra> buscarTodos() {
        return (List<Compra>) compraRepository.findAll();
    }

    public Compra actualizar(Long id, Compra compraActualizada) {
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

        return compraRepository.save(compra);
    }

    public void eliminar(Long id) {
        Compra compra = buscarPorId(id);
        compra.setEstado("CANCELADA");
        compraRepository.save(compra);
    }

    // FILTRADO: Buscar compras con hasta 3 campos
    public List<Compra> buscarConFiltros(String estado, Integer cantidad, String numeroFactura) {
        List<Compra> compras = new ArrayList<>((Collection) compraRepository.findAll());

        // Filtrar por estado
        if (estado != null && !estado.isEmpty()) {
            compras = compras.stream()
                    .filter(c -> c.getEstado().toLowerCase().contains(estado.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filtrar por cantidad
        if (cantidad != null) {
            compras = compras.stream()
                    .filter(c -> c.getCantidad() >= cantidad)
                    .collect(Collectors.toList());
        }

        // Filtrar por número de factura
        if (numeroFactura != null && !numeroFactura.isEmpty()) {
            compras = compras.stream()
                    .filter(c -> c.getNumeroFactura().toLowerCase().contains(numeroFactura.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return compras;
    }
}